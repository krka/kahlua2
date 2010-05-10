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

package se.krka.kahlua.converter;

import java.util.ArrayList;
import java.util.List;

public class MultiJavaToLuaConverter<JavaType> implements JavaToLuaConverter<JavaType> {
    private final List<JavaToLuaConverter> converters = new ArrayList<JavaToLuaConverter>();
    private final Class<JavaType> clazz;

    public MultiJavaToLuaConverter(Class<JavaType> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<JavaType> getJavaType() {
        return clazz;
    }

    @Override
    public Object fromJavaToLua(JavaType javaObject) {
        for (JavaToLuaConverter converter : converters) {
            Object res = converter.fromJavaToLua(javaObject);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void add(JavaToLuaConverter converter) {
        converters.add(converter);
    }
}
