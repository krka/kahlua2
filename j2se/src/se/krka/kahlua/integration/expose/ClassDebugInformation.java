/*
 Copyright (c) 2010 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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

import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.processor.ClassParameterInformation;
import se.krka.kahlua.integration.processor.DescriptorUtil;
import se.krka.kahlua.integration.processor.MethodParameterInformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDebugInformation {
    private final Map<String, MethodDebugInformation> methods = new HashMap<String, MethodDebugInformation>();

    public ClassDebugInformation(Class<?> clazz, ClassParameterInformation parameterInfo) {
        for (Method method : clazz.getMethods()) {
            Annotation[] methodAnnotations = method.getAnnotations();
            String defaultName = method.getName();
            int modifiers = method.getModifiers();
            Class<?>[] parameterTypes = method.getParameterTypes();
            String descriptor = DescriptorUtil.getDescriptor(method);
            Class<?> returnTypeClass = method.getReturnType();

            addMethod(parameterInfo, methodAnnotations, defaultName, parameterTypes, descriptor, returnTypeClass, isStatic(modifiers));
        }
        for (Constructor constructor : clazz.getConstructors()) {
            Annotation[] methodAnnotations = constructor.getAnnotations();
            String defaultName = "new";
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            String descriptor = DescriptorUtil.getDescriptor(constructor);
            Class<?> returnTypeClass = clazz;

            addMethod(parameterInfo, methodAnnotations, defaultName, parameterTypes, descriptor, returnTypeClass, true);
        }
    }

    private void addMethod(ClassParameterInformation parameterInfo, Annotation[] methodAnnotations, String defaultName, Class<?>[] parameterTypes, String descriptor, Class<?> returnTypeClass, boolean isStatic) {
        MethodParameterInformation parameterNames = parameterInfo.methods.get(descriptor);

        String luaName = getName(methodAnnotations, defaultName);
        boolean isMethod = !isGlobal(methodAnnotations, isStatic);

        List<MethodParameter> parameters = new ArrayList<MethodParameter>();


        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            String name = parameterNames.getName(i);
            String typeName = type.getName();
            String description = getDescription(methodAnnotations);
            parameters.add(new MethodParameter(name, typeName, description));
        }

        String returnType = returnTypeClass.getName();
        String returnDescription = getDescription(methodAnnotations);
        MethodDebugInformation debugInfo = new MethodDebugInformation(luaName, isMethod, parameters, returnType, returnDescription);
        methods.put(descriptor, debugInfo);
    }

    private static String getDescription(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Desc) {
                return ((Desc) annotation).value();
            }
        }
        return null;
    }

    private static boolean isStatic(int modifiers) {
        return (modifiers & Modifier.STATIC) != 0;
    }

    private static boolean isGlobal(Annotation[] annotations, boolean defaultValue) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof LuaMethod) {
                return ((LuaMethod) annotation).global();
            }
        }
        return defaultValue;
    }

    private static String getName(Annotation[] annotations, String defaultName) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof LuaMethod) {
                String name = ((LuaMethod) annotation).name();
                if (name != null && name.length() > 0) {
                    return name;
                }
            }
        }
        return defaultName;
    }

    public Map<String, MethodDebugInformation> getMethods() {
        return methods;
    }
}
