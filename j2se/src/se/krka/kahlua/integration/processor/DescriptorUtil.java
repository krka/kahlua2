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

package se.krka.kahlua.integration.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/** @exclude */
public class DescriptorUtil {
    public static String getDescriptor(
            String methodName,
            List<? extends VariableElement> parameters) {
        String parameterString = "";
        for (VariableElement parameter : parameters) {
            parameterString += ":" + parameter.asType().toString();
        }

        return methodName + parameterString;
    }

    public static String getDescriptor(Constructor constructor) {
        String parameters = getParameters(constructor.getParameterTypes());
        return "new" + parameters;
    }

    public static String getDescriptor(Method method) {
        String parameters = getParameters(method.getParameterTypes());
        return method.getName() + parameters;
    }

    private static String getParameters(Class<?>[] parameterTypes) {
        String parameters = "";
        for (Class clazz : parameterTypes) {
            parameters += ":" + clazz.getName();
        }
        return parameters;
    }

}
