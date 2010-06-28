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

import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @exclude */
public class TypeUtil {
    private static final Pattern pattern = Pattern.compile("([\\.a-z0-9]*)\\.([A-Za-z][A-Za-z0-9_]*)");
    public static String removePackages(String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.replaceAll("$2");
    }

    public static String getClassName(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz.isArray()) {
                return getClassName(clazz.getComponentType()) + "[]";
            }
            return clazz.getName();
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] upper = wildcardType.getUpperBounds();
            Type[] lower = wildcardType.getLowerBounds();
            return handleBounds("?", upper, lower);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type[] args = paramType.getActualTypeArguments();
            String raw = getClassName(paramType.getRawType());
            if (args.length == 0) {
                return raw;
            }
            StringBuilder builder = new StringBuilder(raw);
            builder.append("<");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(getClassName(args[i]));
            }
            builder.append(">");
            return builder.toString();
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            return typeVariable.getName();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return getClassName(arrayType.getGenericComponentType()) + "[]";
        }

        System.out.println("got unknown: " + type + ", " + type.getClass());
        return "unknown";
    }

    static String handleBounds(String s, Type[] upper, Type[] lower) {
        if (upper != null) {
            if (upper.length == 1 && upper[0] == Object.class) {
                return s;
            }
            if (upper.length >= 1) {
                StringBuilder list = new StringBuilder();
                boolean first = true;
                for (Type typeExtends : upper) {
                    if (first) {
                        first = false;
                    } else {
                        list.append(", ");
                    }
                    list.append(getClassName(typeExtends));
                }
                return s + " extends " + list.toString();
            }
        }
        if (lower != null) {
            if (lower.length > 0) {
                StringBuilder list = new StringBuilder();
                boolean first = true;
                for (Type typeExtends : lower) {
                    if (first) {
                        first = false;
                    } else {
                        list.append(", ");
                    }
                    list.append(getClassName(typeExtends));
                }
                return s + " super " + list.toString();
            }
        }
        return "unknown type";
    }
}
