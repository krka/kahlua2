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

public class MultiLuaToJavaConverter<LuaType, JavaType> implements LuaToJavaConverter<LuaType, JavaType> {
    private final List<LuaToJavaConverter<LuaType, JavaType>> converters = new ArrayList<LuaToJavaConverter<LuaType,JavaType>>();
    private final Class<LuaType> luaType;
    private final Class<JavaType> javaType;

    public MultiLuaToJavaConverter(Class<LuaType> luaType, Class<JavaType> javaType) {
        this.luaType = luaType;
        this.javaType = javaType;
    }

    @Override
    public Class<LuaType> getLuaType() {
        return luaType;
    }

    @Override
    public Class<JavaType> getJavaType() {
        return javaType;
    }

    @Override
    public JavaType fromLuaToJava(LuaType luaObject, Class<JavaType> javaClass) {
        for (LuaToJavaConverter<LuaType, JavaType> converter : converters) {
            JavaType res = converter.fromLuaToJava(luaObject, javaClass);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void add(LuaToJavaConverter<LuaType, JavaType> converter) {
        converters.add(converter);
    }
}
