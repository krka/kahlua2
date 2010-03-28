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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.krka.kahlua.vm.LuaTable;
import se.krka.kahlua.vm.LuaTableImpl;

public class LuaTableConverter  {
	private LuaTableConverter() {
	}
	
	@SuppressWarnings("unchecked")
	public static void install(final LuaConverterManager manager) {
		manager.addJavaConverter(new JavaToLuaConverter<List>() {
			public Object fromJavaToLua(List javaObject) throws LuaConversionError {
				LuaTableImpl t = new LuaTableImpl();
				int i = 0;
				for (Object o: javaObject) {
					i++;
					t.rawset(i, manager.fromJavaToLua(o));
				}
				return t;
			}

			public Class<List> getJavaType() {
				return List.class;
			}
		});
		manager.addJavaConverter(new JavaToLuaConverter<Map>() {
			public Object fromJavaToLua(Map javaObject) throws LuaConversionError {
				Map<Object, Object> map = javaObject;
				LuaTableImpl t = new LuaTableImpl();
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
		manager.addLuaConverter(new LuaToJavaConverter<LuaTable, List>() {
			public List<Object> fromLuaToJava(LuaTable luaObject) throws IllegalArgumentException {
				int n = luaObject.len();
				List<Object> list = new ArrayList<Object>(n);
				for (int i = 1; i <= n; i++) {
					Object value = luaObject.rawget(i);
					list.add(value);
				}
				return list;
			}

			public Class<List> getJavaType() {
				return List.class;
			}

			public Class<LuaTable> getLuaType() {
				return LuaTable.class;
			}
		});
		manager.addLuaConverter(new LuaToJavaConverter<LuaTable, Map>() {
			public Map fromLuaToJava(LuaTable luaObject) throws IllegalArgumentException {
				Map map = new HashMap();
				Object key = null;
				while (true) {
					key = luaObject.next(key);
					if (key == null) {
						return map;
					}
					Object value = luaObject.rawget(key);
					map.put(key, value);
				}
			}

			public Class<Map> getJavaType() {
				return Map.class;
			}

			public Class<LuaTable> getLuaType() {
				return LuaTable.class;
			}
		});
	}
}
