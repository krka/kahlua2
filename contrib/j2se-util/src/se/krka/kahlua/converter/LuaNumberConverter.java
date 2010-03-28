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

package se.krka.kahlua.converter;

public class LuaNumberConverter {	
	private LuaNumberConverter() {
	}
	
	public static void install(LuaConverterManager manager) {
		manager.addLuaConverter(new LuaToJavaConverter<Double, Long>() {
			public Long fromLuaToJava(Double luaObject) {
				return new Long(luaObject.longValue());
			}
			public Class<Long> getJavaType() {
				return Long.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<Double, Integer>() {
			public Integer fromLuaToJava(Double luaObject) {
				return new Integer(luaObject.intValue());
			}
			public Class<Integer> getJavaType() {
				return Integer.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<Double, Float>() {
			public Float fromLuaToJava(Double luaObject) {
				return new Float(luaObject.floatValue());
			}
			public Class<Float> getJavaType() {
				return Float.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<Double, Byte>() {
			public Byte fromLuaToJava(Double luaObject) {
				return new Byte(luaObject.byteValue());
			}
			public Class<Byte> getJavaType() {
				return Byte.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<Double, Character>() {
			public Character fromLuaToJava(Double luaObject) {
				return new Character((char) luaObject.intValue());
			}
			public Class<Character> getJavaType() {
				return Character.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<Double, Short>() {
			public Short fromLuaToJava(Double luaObject) {
				return new Short(luaObject.shortValue());
			}
			public Class<Short> getJavaType() {
				return Short.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addJavaConverter(new JavaToLuaConverter<Number>() {
			public Object fromJavaToLua(Number javaObject) {
				return new Double(javaObject.doubleValue());
			}

			public Class<Number> getJavaType() {
				return Number.class;
			}
		});
		manager.addJavaConverter(new JavaToLuaConverter<Boolean>() {
			public Object fromJavaToLua(Boolean javaObject) {
				return Boolean.valueOf(javaObject.booleanValue());
			}

			public Class<Boolean> getJavaType() {
				return Boolean.class;
			}
		});
	}
}
