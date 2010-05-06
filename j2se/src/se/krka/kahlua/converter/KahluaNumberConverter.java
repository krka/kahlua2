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

public class KahluaNumberConverter {	
	private KahluaNumberConverter() {
	}

    public static void install(KahluaConverterManager manager) {
		manager.addLuaConverter(new LuaToJavaConverter<Double, Long>() {
			public Long fromLuaToJava(Double luaObject, Class<Long> javaClass) {
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
			public Integer fromLuaToJava(Double luaObject, Class<Integer> javaClass) {
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
			public Float fromLuaToJava(Double luaObject, Class<Float> javaClass) {
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
			public Byte fromLuaToJava(Double luaObject, Class<Byte> javaClass) {
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
			public Character fromLuaToJava(Double luaObject, Class<Character> javaClass) {
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
			public Short fromLuaToJava(Double luaObject, Class<Short> javaClass) {
				return new Short(luaObject.shortValue());
			}
			public Class<Short> getJavaType() {
				return Short.class;
			}
			public Class<Double> getLuaType() {
				return Double.class;
			}
		});
		manager.addJavaConverter(new NumberToLuaConverter(Double.class));
        manager.addJavaConverter(new NumberToLuaConverter(Float.class));
        manager.addJavaConverter(new NumberToLuaConverter(Integer.class));
        manager.addJavaConverter(new NumberToLuaConverter(Long.class));
        manager.addJavaConverter(new NumberToLuaConverter(Short.class));
        manager.addJavaConverter(new NumberToLuaConverter(Byte.class));
        manager.addJavaConverter(new NumberToLuaConverter(Character.class));
        manager.addJavaConverter(new NumberToLuaConverter(double.class));
        manager.addJavaConverter(new NumberToLuaConverter(float.class));
        manager.addJavaConverter(new NumberToLuaConverter(int.class));
        manager.addJavaConverter(new NumberToLuaConverter(long.class));
        manager.addJavaConverter(new NumberToLuaConverter(short.class));
        manager.addJavaConverter(new NumberToLuaConverter(byte.class));
        manager.addJavaConverter(new NumberToLuaConverter(char.class));

		manager.addJavaConverter(new JavaToLuaConverter<Boolean>() {
			public Object fromJavaToLua(Boolean javaObject) {
				return Boolean.valueOf(javaObject.booleanValue());
			}

			public Class<Boolean> getJavaType() {
				return Boolean.class;
			}
		});
	}

    private static class NumberToLuaConverter<T extends Number> implements JavaToLuaConverter<T> {
        private final Class<T> clazz;

        public NumberToLuaConverter(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Object fromJavaToLua(T javaObject) {
            return new Double(javaObject.doubleValue());
        }

        public Class<T> getJavaType() {
            return clazz;
        }
    }
}
