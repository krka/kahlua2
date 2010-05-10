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

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.Platform;

public class KahluaTableConverter {

	private final Platform platform;

	public KahluaTableConverter(Platform platform) {
		this.platform = platform;
	}

    @SuppressWarnings("unchecked")
	public void install(final KahluaConverterManager manager) {
		manager.addJavaConverter(new CollectionToLuaConverter(manager, Collection.class));
        manager.addLuaConverter(new CollectionToJavaConverter(Collection.class));

		manager.addJavaConverter(new JavaToLuaConverter<Map>() {
			public Object fromJavaToLua(Map javaObject) {
				Map<Object, Object> map = javaObject;
                KahluaTable t = platform.newTable();
				for (Entry<Object, Object> entry: map.entrySet()) {
					Object key = manager.fromJavaToLua(entry.getKey());
					Object value = manager.fromJavaToLua(entry.getValue());
					t.rawset(key, value);
				}
				return t;
			}

			public Class<Map> getJavaType() {
				return Map.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<KahluaTable, Map>() {
			public Map fromLuaToJava(KahluaTable luaObject, Class<Map> javaClass) throws IllegalArgumentException {
                KahluaTableIterator iterator = luaObject.iterator();
                Map map = new HashMap();
                while (iterator.advance()) {
                    Object key = iterator.getKey();
                    Object value = iterator.getValue();
                    map.put(key, value);
                }
                return map;
			}

			public Class<Map> getJavaType() {
				return Map.class;
			}

			public Class<KahluaTable> getLuaType() {
				return KahluaTable.class;
			}
		});

        manager.addJavaConverter(new JavaToLuaConverter<Object>() {
                    public Object fromJavaToLua(Object javaObject) {
                        if (javaObject.getClass().isArray()) {
                            KahluaTable t = platform.newTable();
                            int n = Array.getLength(javaObject);
                            for (int i = 0; i < n; i++) {
                                Object value = Array.get(javaObject, i);
                                t.rawset(i + 1, manager.fromJavaToLua(value));
                            }
                            return t;
                        }
                        return null;
                    }

                    public Class<Object> getJavaType() {
                        return Object.class;
                    }
                });
                manager.addLuaConverter(new LuaToJavaConverter<KahluaTable, Object>() {
                    public Object fromLuaToJava(KahluaTable luaObject, Class<Object> javaClass) throws IllegalArgumentException {
                        if (javaClass.isArray()) {
                            List list = manager.fromLuaToJava(luaObject, List.class);
                            return list.toArray();
                        }
                        return null;
                    }

                    public Class<Object> getJavaType() {
                        return Object.class;
                    }

                    public Class<KahluaTable> getLuaType() {
                        return KahluaTable.class;
                    }
                });
	}

    private class CollectionToLuaConverter<T extends Iterable> implements JavaToLuaConverter<T> {
        private final Class<T> clazz;
        private final KahluaConverterManager manager;

        public CollectionToLuaConverter(KahluaConverterManager manager, Class<T> clazz) {
            this.manager = manager;
            this.clazz = clazz;
        }

        public Object fromJavaToLua(T javaObject) {
KahluaTable t = platform.newTable();
            int i = 0;
            for (Object o: javaObject) {
                i++;
                t.rawset(i, manager.fromJavaToLua(o));
            }
            return t;
        }

        public Class<T> getJavaType() {
            return clazz;
        }
    }

    
    private static class CollectionToJavaConverter<T> implements LuaToJavaConverter<KahluaTable, T> {
        private final Class<T> javaClass;

        private CollectionToJavaConverter(Class<T> javaClass) {
            this.javaClass = javaClass;
        }

        public T fromLuaToJava(KahluaTable luaObject, Class<T> javaClass) throws IllegalArgumentException {
            int n = luaObject.len();
            List list = new ArrayList(n);
            for (int i = 1; i <= n; i++) {
                Object value = luaObject.rawget(i);
                list.add(value);
            }
            return (T) list;
        }

        public Class<T> getJavaType() {
            return javaClass;
        }

        public Class<KahluaTable> getLuaType() {
            return KahluaTable.class;
        }
    }
}
