/*
 Copyright (c) 2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package se.krka.kahlua.integration.expose;

import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.integration.annotations.LuaClass;
import se.krka.kahlua.integration.annotations.LuaConstructor;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.caller.ConstructorCaller;
import se.krka.kahlua.integration.expose.caller.MethodCaller;
import se.krka.kahlua.integration.processor.LuaClassDebugInformation;
import se.krka.kahlua.integration.processor.LuaMethodDebugInformation;
import se.krka.kahlua.vm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;

/**
 * A tool to automatically expose java classes and
 * methods to a lua state
 * NOTE: This tool requires annotations (java 1.5 or higher) to work
 * and is therefore not supported in J2ME.
 */
public class LuaJavaClassExposer {
    private final LuaConverterManager manager;
    private final Platform platform;
    private final KahluaTable environment;

    public LuaJavaClassExposer(LuaConverterManager manager, Platform platform, KahluaTable environment) {
        this.manager = manager;
        this.platform = platform;
        this.environment = environment;
    }

    public KahluaTable getClassDebugInformation() {
        KahluaTable map = KahluaUtil.getOrCreateTable(environment, platform, "__classdebuginfo");
        return map;
    }

    @LuaMethod(global = true)
    public KahluaTable getExposedClasses() {
        KahluaTable t = platform.newTable();
        KahluaTable classDebugInformation = getClassDebugInformation();
        KahluaTableIterator iterator = classDebugInformation.iterator();
        while (iterator.advance()) {
            Class clazz = (Class) iterator.getKey();
            LuaClassDebugInformation debugInfo = (LuaClassDebugInformation) iterator.getValue();
            KahluaTable classTable = platform.newTable();
            boolean hasDebug = debugInfo != null && debugInfo != LuaClassDebugInformation.NULL;
            classTable.rawset("hasDebug", KahluaUtil.toBoolean(hasDebug));
            if (hasDebug) {
                KahluaTable methods = platform.newTable();
                KahluaTable functions = platform.newTable();

                for (Entry<String, LuaMethodDebugInformation> entry : debugInfo.methods.entrySet()) {
                    LuaMethodDebugInformation debug = entry.getValue();
                    if (debug.isMethod()) {
                        methods.rawset(debug.getName(), debug);
                    } else {
                        functions.rawset(debug.getName(), debug);
                    }
                }

                classTable.rawset("methods", methods);
                classTable.rawset("functions", functions);
            }
            if (classDebugInformation.rawget(clazz.getSuperclass()) != null) {
                classTable.rawset("super", clazz.getSuperclass().getName());
            }
            t.rawset(clazz.getName(), classTable);
        }
        return t;
    }

    @LuaMethod(global = true)
    public LuaMethodDebugInformation getDebugInfo(Object functionObject) {
        if (!(functionObject instanceof LuaJavaInvoker)) {
            return null;
        }
        LuaJavaInvoker methodObject = (LuaJavaInvoker) functionObject;
        return methodObject.getMethodDebugData();
    }

    public void exposeClass(Class<?> clazz) {
        if (!isLuaClass(clazz)) {
            return;
        }
        if (!isExposed(clazz)) {
            readDebugData(clazz);
            setupMetaTables(clazz);

            populateMethods(clazz);
        }
    }

    private KahluaTable getMetaTable(Class<?> clazz) {
        KahluaTable metatables = KahluaUtil.getClassMetatables(environment, platform);
        return (KahluaTable) metatables.rawget(clazz);
    }

    private KahluaTable getIndexTable(KahluaTable metaTable) {
        if (metaTable == null) {
            return null;
        }

        Object indexObject = metaTable.rawget("__index");
        if (indexObject == null) {
            return null;
        }
        if (indexObject instanceof KahluaTable) {
            return (KahluaTable) indexObject;
        }
        return null;
    }

    /**
     * Creates a global variable in the LuaState that points to a function
     * which calls the specified method on the owner object.
     * <p/>
     * The name of the global variable is the same as the name of the method.
     *
     * @param environment typically LuaState.getEnvironment()
     * @param owner
     * @param method
     */
    public void exposeGlobalObjectFunction(KahluaTable environment, Object owner, Method method) {
        exposeGlobalObjectFunction(environment, owner, method, method.getName());
    }

    /**
     * Creates a global variable in the LuaState that points to a function
     * which calls the specified method on the owner object.
     * <p/>
     * The name of the global variable is the same as methodName
     *
     * @param environment typically LuaState.getEnvironment()
     * @param owner
     * @param method
     * @param methodName  the name of the method in Lua
     */
    public void exposeGlobalObjectFunction(KahluaTable environment, Object owner, Method method, String methodName) {
        Class<? extends Object> clazz = owner.getClass();
        readDebugData(clazz);
        environment.rawset(methodName, getInvoker(owner, method, methodName, clazz));
    }

    private LuaJavaInvoker getInvoker(Object owner, Method method, String methodName, Class<? extends Object> clazz) {
        return new LuaJavaInvoker(this, manager, clazz, methodName, new MethodCaller(method, owner, false));
    }

    public void exposeGlobalClassFunction(KahluaTable environment, Class<?> clazz, Constructor<?> constructor, String methodName) {
        readDebugData(clazz);
        environment.rawset(methodName, getInvoker(clazz, constructor, methodName));
    }

    private LuaJavaInvoker getInvoker(Class<?> clazz, Constructor<?> constructor, String methodName) {
        return new LuaJavaInvoker(this, manager, clazz, methodName, new ConstructorCaller(constructor));
    }

    public void exposeGlobalClassFunction(KahluaTable environment, Class<?> clazz, Method method, String methodName) {
        readDebugData(clazz);
        if (Modifier.isStatic(method.getModifiers())) {
            environment.rawset(methodName, getInvoker(clazz, method, methodName));
        }
    }

    private LuaJavaInvoker getInvoker(Class<?> clazz, Method method, String methodName) {
        return getInvoker(null, method, methodName, clazz);
    }

    /**
     * Exposes an object method for a specific class, which means that that method
     * will be accessible from all objects in Lua of exactly that class.
     * <p/>
     * Usage:
     * If object is of type clazz, then the method can be called from Lua with:
     * object:methodName(args)
     *
     * @param clazz
     * @param method
     */
    public void exposeMethod(Class<?> clazz, Method method) {
        exposeMethod(clazz, method, method.getName());
    }

    /**
     * Exposes an object method for a specific class, which means that that method
     * will be accessible from all objects in Lua of exactly that class.
     * <p/>
     * Usage:
     * If object is of type clazz, then the method can be called from Lua with:
     * object:methodName(args)
     *
     * @param clazz
     * @param method
     * @param methodName what it should be called in Lua. Does not have to match method.getName()
     */
    public void exposeMethod(Class<?> clazz, Method method, String methodName) {
        readDebugData(clazz);
        if (!isExposed(clazz)) {
            setupMetaTables(clazz);
        }
        KahluaTable metaTable = getMetaTable(clazz);
        KahluaTable indexTable = getIndexTable(metaTable);
        indexTable.rawset(methodName, new LuaJavaInvoker(this, manager, clazz, methodName, new MethodCaller(method, null, true)));
    }

    private void setupMetaTables(Class<?> clazz) {
        Class<?> superClazz = getSuperClass(clazz);
        exposeClass(superClazz);

        KahluaTable superMetaTable = getMetaTable(superClazz);

        KahluaTable metatable = platform.newTable();
        KahluaTable indexTable = platform.newTable();
		metatable.rawset("__index", indexTable);
		if (superMetaTable != null) {
			metatable.rawset("__newindex", superMetaTable.rawget("__newindex"));
		}
        indexTable.setMetatable(superMetaTable);
        KahluaUtil.getClassMetatables(environment, platform).rawset(clazz, metatable);
    }

    public void exposeGlobalFunctions(Object object) {
        Class<?> clazz = object.getClass();
        readDebugData(clazz);
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(LuaMethod.class)) {
                LuaMethod luaMethod = method.getAnnotation(LuaMethod.class);

                String methodName;
                if (luaMethod.name().equals(LuaMethod.UNASSIGNED)) {
                    methodName = method.getName();
                } else {
                    methodName = luaMethod.name();
                }
                if (luaMethod.global()) {
                    exposeGlobalObjectFunction(environment, object, method, methodName);
                }
            }
        }
    }

    public void exposeLikeJava(Class clazz, KahluaTable staticBase) {
        if (clazz == null || isExposed(clazz)) {
            return;
        }
        setupMetaTables(clazz);

        KahluaTable container = KahluaUtil.getOrCreateTable(staticBase, platform, clazz.getSimpleName());
        container.rawset("class", clazz);
        for (Method method : clazz.getMethods()) {
            String name = method.getName();
            if (Modifier.isPublic(method.getModifiers())) {
                if (Modifier.isStatic(method.getModifiers())) {
                    container.rawset(name, getInvoker(clazz, method, name));
                } else {
                    exposeMethod(clazz, method, name);
                }
            }
        }
        for (Field field : clazz.getFields()) {
            String name = field.getName();
            if (Modifier.isPublic(field.getModifiers())) {
                if (Modifier.isStatic(field.getModifiers())) {
                    try {
                        container.rawset(name, field.get(clazz));
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        }
        for (Constructor constructor : clazz.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                container.rawset("new", getInvoker(clazz, constructor, "new"));
            }
        }
    }

    private void populateMethods(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            LuaConstructor annotation = constructor.getAnnotation(LuaConstructor.class);
            if (annotation != null) {
                String methodName = annotation.name();
                exposeGlobalClassFunction(environment, clazz, constructor, methodName);
            }
        }
        for (Method method : clazz.getMethods()) {
            LuaMethod luaMethod = method.getAnnotation(LuaMethod.class);
            if (luaMethod != null) {

                String methodName;
                if (luaMethod.name().equals(LuaMethod.UNASSIGNED)) {
                    methodName = method.getName();
                } else {
                    methodName = luaMethod.name();
                }
                if (luaMethod.global()) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        exposeGlobalClassFunction(environment, clazz, method, methodName);
                    }
                } else {
                    exposeMethod(clazz, method, methodName);
                }
            }
        }
    }

    private Class<?> getSuperClass(Class<?> clazz) {
        Class<?> superClazz = clazz.getSuperclass();
        while (superClazz != null && superClazz != Object.class && !isLuaClass(superClazz)) {
            superClazz = superClazz.getSuperclass();
        }
        return superClazz;
    }

    private boolean isLuaClass(Class<?> clazz) {
        return clazz != null && clazz.isAnnotationPresent(LuaClass.class);
    }

    public boolean isExposed(Class<?> clazz) {
        return clazz != null && getMetaTable(clazz) != null;
    }

    LuaClassDebugInformation getDebugdata(Class<?> clazz) {
        return (LuaClassDebugInformation) getClassDebugInformation().rawget(clazz);
    }

    private void readDebugData(Class<?> clazz) {
        if (getDebugdata(clazz) == null) {
            LuaClassDebugInformation debugInfo = null;
            try {
                debugInfo = LuaClassDebugInformation.getFromStream(clazz);
            } catch (Exception e) {
            }
            if (debugInfo == null) {
                debugInfo = LuaClassDebugInformation.NULL;
            }
            KahluaTable classDebugInformation = getClassDebugInformation();
			classDebugInformation.rawset(clazz, debugInfo);
		}
	}
}
