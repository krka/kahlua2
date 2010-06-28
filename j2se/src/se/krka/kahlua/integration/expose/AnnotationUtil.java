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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/** @exclude */
public class AnnotationUtil {
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotation) {
        return getAnnotation(method.getDeclaringClass(), method.getName(), method.getParameterTypes(), annotation);
    }

    private static <T extends Annotation> T getAnnotation(Class<?> clazz, String name, Class<?>[] types, Class<T> annotationType) {
        if (clazz == null) {
            return null;
        }
        try {
            Method method = clazz.getMethod(name, types);
            T annotation = method.getAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
            for (Class<?> subClass : clazz.getInterfaces()) {
                annotation = getAnnotation(subClass, name, types, annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
            return getAnnotation(clazz.getSuperclass(), name, types, annotationType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
