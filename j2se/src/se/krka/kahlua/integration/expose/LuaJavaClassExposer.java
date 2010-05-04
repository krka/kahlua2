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
import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaClass;
import se.krka.kahlua.integration.annotations.LuaConstructor;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.caller.ConstructorCaller;
import se.krka.kahlua.integration.expose.caller.MethodCaller;
import se.krka.kahlua.integration.processor.ClassParameterInformation;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * A tool to automatically expose java classes and
 * methods to a lua thread
 * NOTE: This tool requires annotations (java 1.5 or higher) to work
 * and is therefore not supported in J2ME.
 */
public class LuaJavaClassExposer {
    private final static Object DEBUGINFO_KEY = new Object();
    private final LuaConverterManager manager;
    private final Platform platform;
    private final KahluaTable environment;

    public LuaJavaClassExposer(LuaConverterManager manager, Platform platform, KahluaTable environment) {
        this.manager = manager;
        this.platform = platform;
        this.environment = environment;
    }

    public Map<Class<?>, ClassDebugInformation> getClassDebugInformation() {
        Object classMap = environment.rawget(DEBUGINFO_KEY);
        if (classMap == null || !(classMap instanceof Map)) {
            classMap = new HashMap<Class<?>, ClassDebugInformation>();
            environment.rawset(DEBUGINFO_KEY, classMap);
        }
        return (Map<Class<?>, ClassDebugInformation>) classMap;
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
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, environment);
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
     * Creates a global variable in the environment that points to a function
     * which calls the specified method on the owner object.
     * <p/>
     * The name of the global variable is the same as the name of the method.
     *
     * @param environment
     * @param owner
     * @param method
     */
    public void exposeGlobalObjectFunction(KahluaTable environment, Object owner, Method method) {
        exposeGlobalObjectFunction(environment, owner, method, method.getName());
    }

    /**
     * Creates a global variable in the environment that points to a function
     * which calls the specified method on the owner object.
     * <p/>
     * The name of the global variable is the same as methodName
     *
     * @param environment
     * @param owner
     * @param method
     * @param methodName  the name of the method in Lua
     */
    public void exposeGlobalObjectFunction(KahluaTable environment, Object owner, Method method, String methodName) {
        Class<? extends Object> clazz = owner.getClass();
        readDebugData(clazz);
        LuaJavaInvoker invoker = getMethodInvoker(clazz, method, methodName, owner, false);
        addInvoker(environment, methodName, invoker);
    }

    public void exposeGlobalClassFunction(KahluaTable environment, Class<?> clazz, Constructor<?> constructor, String methodName) {
        readDebugData(clazz);
        LuaJavaInvoker invoker = getConstructorInvoker(clazz, constructor, methodName);
        addInvoker(environment, methodName, invoker);
    }

    private LuaJavaInvoker getMethodInvoker(Class<?> clazz, Method method, String methodName, Object owner, boolean hasSelf) {
        return new LuaJavaInvoker(this, manager, clazz, methodName, new MethodCaller(method, owner, hasSelf));
    }

    private LuaJavaInvoker getConstructorInvoker(Class<?> clazz, Constructor<?> constructor, String methodName) {
        return new LuaJavaInvoker(this, manager, clazz, methodName, new ConstructorCaller(constructor));
    }

    private LuaJavaInvoker getMethodInvoker(Class<?> clazz, Method method, String methodName) {
        return getMethodInvoker(clazz, method, methodName, null, true);
    }

    private LuaJavaInvoker getGlobalInvoker(Class<?> clazz, Method method, String methodName) {
        return getMethodInvoker(clazz, method, methodName, null, false);
    }

    public void exposeGlobalClassFunction(KahluaTable environment, Class<?> clazz, Method method, String methodName) {
        readDebugData(clazz);
        if (Modifier.isStatic(method.getModifiers())) {
            addInvoker(environment, methodName, getGlobalInvoker(clazz, method, methodName));
        }
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

        LuaJavaInvoker newInvoker = getMethodInvoker(clazz, method, methodName);
        addInvoker(indexTable, methodName, newInvoker);
    }

    private void addInvoker(KahluaTable indexTable, String methodName, LuaJavaInvoker invoker) {
        Object current = indexTable.rawget(methodName);
        if (current != null) {
            if (current instanceof LuaJavaInvoker) {
                MultiLuaJavaInvoker multiInvoker = new MultiLuaJavaInvoker();
                multiInvoker.addInvoker((LuaJavaInvoker) current);
                multiInvoker.addInvoker(invoker);
                indexTable.rawset(methodName, multiInvoker);
            } else if (current instanceof MultiLuaJavaInvoker) {
                ((MultiLuaJavaInvoker) current).addInvoker(invoker);
            }
        } else {
            indexTable.rawset(methodName, invoker);
        }
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
        KahluaUtil.getClassMetatables(platform, environment).rawset(clazz, metatable);
    }

    public void exposeGlobalFunctions(Object object) {
        Class<?> clazz = object.getClass();
        readDebugData(clazz);
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(LuaMethod.class)) {
                LuaMethod luaMethod = method.getAnnotation(LuaMethod.class);

                String methodName;
                if (luaMethod.name().equals("")) {
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

        String[] packageStructure = clazz.getName().split("\\.");
        KahluaTable container = createTableStructure(staticBase, packageStructure);
        container.rawset("class", clazz);
        if (staticBase.rawget(clazz.getSimpleName()) == null) {
            staticBase.rawset(clazz.getSimpleName(), container);
        }
        for (Method method : clazz.getMethods()) {
            String name = method.getName();
            if (Modifier.isPublic(method.getModifiers())) {
                if (Modifier.isStatic(method.getModifiers())) {
                    exposeGlobalClassFunction(container, clazz, method, name);
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
            int modifiers = constructor.getModifiers();
            if (!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers)) {
                addInvoker(container, "new", getConstructorInvoker(clazz, constructor, "new"));
            }
        }
    }

    private KahluaTable createTableStructure(KahluaTable base, String[] structure) {
        for (String s : structure) {
            base = KahluaUtil.getOrCreateTable(platform, base, s);
        }
        return base;
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
                if (luaMethod.name().equals("")) {
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

    ClassDebugInformation getDebugdata(Class<?> clazz) {
        return getClassDebugInformation().get(clazz);
    }

    private void readDebugData(Class<?> clazz) {
        if (getDebugdata(clazz) == null) {
            ClassParameterInformation parameterInfo = null;
            try {
                parameterInfo = ClassParameterInformation.getFromStream(clazz);
            } catch (Exception e) {
            }
            if (parameterInfo == null) {
                parameterInfo = new ClassParameterInformation(clazz);
            }
            ClassDebugInformation debugInfo = new ClassDebugInformation(clazz, parameterInfo);

            Map<Class<?>, ClassDebugInformation> information = getClassDebugInformation();
            information.put(clazz, debugInfo);
		}
	}

    @LuaMethod(global = true, name = "definition")
    @Desc("returns a string that describes the object")
    public String getFunctionDefinition(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof LuaJavaInvoker) {
            MethodDebugInformation data = ((LuaJavaInvoker) obj).getMethodDebugData();
            return data.toString();
        } else if (obj instanceof MultiLuaJavaInvoker) {
            StringBuilder builder = new StringBuilder();
            for (LuaJavaInvoker invoker : ((MultiLuaJavaInvoker) obj).getInvokers()) {
                builder.append(invoker.getMethodDebugData().toString());
            }
            return builder.toString();
        } else {
            return BaseLib.tostring(obj, KahluaUtil.getWorkerThread(platform, environment));
        }
    }

    public void exposeLikeJavaRecursively(Class<?> clazz, KahluaTable staticBase) {
        if (clazz.isArray()) {
            exposeLikeJavaRecursively(clazz.getComponentType(), staticBase);
        } else if (!isExposed(clazz)) {
            exposeLikeJava(clazz, staticBase);
            for (Method method : clazz.getMethods()) {
                exposeLikeJavaRecursively(method.getReturnType(), staticBase);
                for (Class<?> aClass : method.getParameterTypes()) {
                    exposeLikeJavaRecursively(aClass, staticBase);
                }
            }
        }
    }
}
