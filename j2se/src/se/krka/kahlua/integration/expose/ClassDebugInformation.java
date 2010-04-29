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
import se.krka.kahlua.integration.annotations.LuaConstructor;
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
            LuaMethod methodAnnotation = method.getAnnotation(LuaMethod.class);
            String defaultName = method.getName();
            int modifiers = method.getModifiers();
            Class<?>[] parameterTypes = method.getParameterTypes();
            String descriptor = DescriptorUtil.getDescriptor(method);
            Class<?> returnTypeClass = method.getReturnType();

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Desc descriptionAnnotation = method.getAnnotation(Desc.class);
            addMethod(parameterInfo, parameterTypes, descriptor, returnTypeClass, parameterAnnotations, getName(methodAnnotation, defaultName), !isGlobal(methodAnnotation, isStatic(modifiers)), descriptionAnnotation);
        }
        for (Constructor constructor : clazz.getConstructors()) {
            LuaConstructor methodAnnotation = (LuaConstructor) constructor.getAnnotation(LuaConstructor.class);
            String defaultName = "new";
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            String descriptor = DescriptorUtil.getDescriptor(constructor);
            Class<?> returnTypeClass = clazz;

            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            Desc descriptionAnnotation = (Desc) constructor.getAnnotation(Desc.class);
            addMethod(parameterInfo, parameterTypes, descriptor, returnTypeClass, parameterAnnotations, getName(methodAnnotation, defaultName), true, descriptionAnnotation);
        }
    }

    private void addMethod(
            ClassParameterInformation parameterInfo,
            Class<?>[] parameterTypes,
            String descriptor,
            Class<?> returnTypeClass,
            Annotation[][] parameterAnnotations,
            String luaName,
            boolean method,
            Desc descriptionAnnotation) {
        MethodParameterInformation parameterNames = parameterInfo.methods.get(descriptor);

        List<MethodParameter> parameters = new ArrayList<MethodParameter>();


        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            String name = parameterNames.getName(i);
            String typeName = type.getName();
            String description = getDescription(parameterAnnotations[i]);
            parameters.add(new MethodParameter(name, typeName, description));
        }

        String returnType = returnTypeClass.getName();
        String returnDescription = getDescription(descriptionAnnotation);
        MethodDebugInformation debugInfo = new MethodDebugInformation(luaName, method, parameters, returnType, returnDescription);
        methods.put(descriptor, debugInfo);
    }

    private String getDescription(Annotation[] parameterAnnotation) {
        for (Annotation annotation : parameterAnnotation) {
            if (annotation != null && annotation instanceof Desc) {
                return getDescription((Desc) annotation);
            }
        }
        return null;
    }

    private static String getDescription(Desc annotation) {
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }

    private static boolean isStatic(int modifiers) {
        return (modifiers & Modifier.STATIC) != 0;
    }

    private static boolean isGlobal(LuaMethod annotation, boolean defaultValue) {
        if (annotation != null) {
            return annotation.global();
        }
        return defaultValue;
    }

    private static String getName(LuaMethod annotation, String defaultName) {
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && name.length() > 0) {
                return name;
            }
        }
        return defaultName;
    }

    private static String getName(LuaConstructor annotation, String defaultName) {
        if (annotation != null) {
            String name = annotation.name();
            if (name != null && name.length() > 0) {
                return name;
            }
        }
        return defaultName;
    }

    public Map<String, MethodDebugInformation> getMethods() {
        return methods;
    }
}
