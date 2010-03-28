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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.integration.annotations.LuaClass;
import se.krka.kahlua.integration.annotations.LuaConstructor;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.caller.ConstructorCaller;
import se.krka.kahlua.integration.expose.caller.MethodCaller;
import se.krka.kahlua.integration.processor.LuaClassDebugInformation;
import se.krka.kahlua.integration.processor.LuaMethodDebugInformation;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;
import se.krka.kahlua.vm.LuaTableImpl;
import se.krka.kahlua.vm.LuaArray;

/**
 * A tool to automatically expose java classes and
 * methods to a lua state
 * NOTE: This tool requires annotations (java 1.5 or higher) to work
 * and is therefore not supported in J2ME.
 */
public class LuaJavaClassExposer {
    private final LuaState state;
    private final LuaConverterManager manager;

    public LuaJavaClassExposer(LuaState state, LuaConverterManager manager) {
        this.state = state;
        this.manager = manager;
    }

    public Map<Class<?>, LuaClassDebugInformation> getClassDebugInformation() {
        final String keyname = "_classdebuginfo";

        LuaTable env = state.getEnvironment();
        Map<Class<?>, LuaClassDebugInformation> value = (Map<Class<?>, LuaClassDebugInformation>) env.rawget(keyname);
        if (value == null) {
            value = new HashMap<Class<?>, LuaClassDebugInformation>();
            env.rawset(keyname, value);
        }
        return value;
    }

    @LuaMethod(global = true)
    public LuaTable getExposedClasses() {
        LuaTable t = new LuaTableImpl();
        Map<Class<?>, LuaClassDebugInformation> classDebugInformation = getClassDebugInformation();
        Set<Class<?>> keyset = classDebugInformation.keySet();
        for (Class<?> clazz : keyset) {
            LuaTable classTable = new LuaTableImpl();
            LuaClassDebugInformation debugInfo = classDebugInformation.get(clazz);
            boolean hasDebug = debugInfo != null && debugInfo != LuaClassDebugInformation.NULL;
            classTable.rawset("hasDebug", LuaState.toBoolean(hasDebug));
            if (hasDebug) {
                LuaTable methods = new LuaTableImpl();
                LuaTable functions = new LuaTableImpl();

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
            if (classDebugInformation.containsKey(clazz.getSuperclass())) {
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
        try {
            state.lock();
            if (!isLuaClass(clazz)) {
                return;
            }
            if (!isExposed(clazz)) {
                readDebugData(clazz);
                setupMetaTables(clazz);

                populateMethods(clazz);
            }
        } finally {
            state.unlock();
        }
    }

    private LuaTable getMetaTable(Class<?> clazz) {
        return state.getClassMetatable(clazz);
    }

    private LuaTable getIndexTable(LuaTable metaTable) {
        if (metaTable == null) {
            return null;
        }

        Object indexObject = metaTable.rawget("__index");
        if (indexObject == null) {
            return null;
        }
        if (indexObject instanceof LuaTable) {
            return (LuaTable) indexObject;
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
    public void exposeGlobalObjectFunction(LuaTable environment, Object owner, Method method) {
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
    public void exposeGlobalObjectFunction(LuaTable environment, Object owner, Method method, String methodName) {
        Class<? extends Object> clazz = owner.getClass();
        readDebugData(clazz);
        state.lock();
        environment.rawset(methodName, new LuaJavaInvoker(this, manager, clazz, methodName, new MethodCaller(method, owner, false)));
        state.unlock();
    }

    public void exposeGlobalClassFunction(LuaTable environment, Class<?> clazz, Constructor<?> constructor, String methodName) {
        readDebugData(clazz);
        state.lock();
        environment.rawset(methodName, new LuaJavaInvoker(this, manager, clazz, methodName, new ConstructorCaller(constructor)));
        state.unlock();
    }

    public void exposeGlobalClassFunction(LuaTable environment, Class<?> clazz, Method method, String methodName) {
        readDebugData(clazz);
        state.lock();
        if (Modifier.isStatic(method.getModifiers())) {
            environment.rawset(methodName, new LuaJavaInvoker(this, manager, clazz, methodName, new MethodCaller(method, null, false)));
        }
        state.unlock();
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
        state.lock();
        if (!isExposed(clazz)) {
            setupMetaTables(clazz);
        }
        LuaTable metaTable = getMetaTable(clazz);
        LuaTable indexTable = getIndexTable(metaTable);
        indexTable.rawset(methodName, new LuaJavaInvoker(this, manager, clazz, methodName, new MethodCaller(method, null, true)));
        state.unlock();
    }

    private void setupMetaTables(Class<?> clazz) {
        Class<?> superClazz = getSuperClass(clazz);
        exposeClass(superClazz);

        LuaTable superMetaTable = getMetaTable(superClazz);

        LuaTable metatable = new LuaTableImpl();
        LuaTable indexTable = new LuaTableImpl();
		metatable.rawset("__index", indexTable);
		if (superMetaTable != null) {
			metatable.rawset("__newindex", superMetaTable.rawget("__newindex"));
		}
        indexTable.setMetatable(superMetaTable);

        state.setClassMetatable(clazz, metatable);
    }

    public void exposeGlobalFunctions(Object object) {
        try {
            Class<?> clazz = object.getClass();
            readDebugData(clazz);
            state.lock();
            LuaTable environment = state.getEnvironment();
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
        } finally {
            state.unlock();
        }
    }

    private void populateMethods(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            LuaConstructor annotation = constructor.getAnnotation(LuaConstructor.class);
            if (annotation != null) {
                String methodName = annotation.name();
                exposeGlobalClassFunction(state.getEnvironment(), clazz, constructor, methodName);
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
                        exposeGlobalClassFunction(state.getEnvironment(), clazz, method, methodName);
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

    private boolean isExposed(Class<?> clazz) {
        return getMetaTable(clazz) != null;
    }

    LuaClassDebugInformation getDebugdata(Class<?> clazz) {
        return getClassDebugInformation().get(clazz);
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
            Map<Class<?>, LuaClassDebugInformation> classDebugInformation = getClassDebugInformation();
			classDebugInformation.put(clazz, debugInfo);
		}
	}
}
