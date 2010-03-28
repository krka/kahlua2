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

package se.krka.kahlua.integration.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import se.krka.kahlua.integration.processor.LuaClassDebugInformation;
import se.krka.kahlua.integration.processor.LuaMethodDebugInformation;

public class ApiDocumentationExporter implements ApiInformation {

	private final Map<Class<?>, LuaClassDebugInformation> classes;
	
	private final Map<Class<?>, List<Class<?>>> classHierarchy = new HashMap<Class<?>, List<Class<?>>>();
	private final List<Class<?>> rootClasses = new ArrayList<Class<?>>();
	private final List<Class<?>> allClasses = new ArrayList<Class<?>>();

	private Comparator<Class<?>> classSorter = new Comparator<Class<?>>() {
		@Override
		public int compare(Class<?> arg0, Class<?> arg1) {
			int c = arg0.getSimpleName().compareTo(arg1.getSimpleName());
			if (c != 0) {
				return c;
			}
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	private Comparator<LuaMethodDebugInformation> methodSorter = new Comparator<LuaMethodDebugInformation>() {
		@Override
		public int compare(LuaMethodDebugInformation arg0, LuaMethodDebugInformation arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	public ApiDocumentationExporter(Map<Class<?>, LuaClassDebugInformation> classes) {
		this.classes = classes;
		setupHierarchy();
	}
	
	public void setupHierarchy() {
		for (Entry<Class<?>, LuaClassDebugInformation> entry: classes.entrySet()) {
			Class<?> clazz = entry.getKey();
			Class<?> zuper = clazz.getSuperclass();
			if (classes.containsKey(zuper)) {
				List<Class<?>> list = classHierarchy.get(zuper);
				if (list == null) {
					list = new ArrayList<Class<?>>();
					classHierarchy.put(zuper, list);
				}
				list.add(clazz);
			} else {
				rootClasses.add(clazz);
			}
			allClasses.add(clazz);
		}
		
		Collections.sort(allClasses, classSorter);
		Collections.sort(rootClasses, classSorter);
		for (List<Class<?>> list: classHierarchy.values()) {
			Collections.sort(list, classSorter);
		}
	}
	
	public List<Class<?>> getAllClasses() {
		return allClasses;
	}

	public List<Class<?>> getChildrenForClass(Class<?> clazz) {
		List<Class<?>> list = classHierarchy.get(clazz);
		if (list != null) {
			return list;
		}
		return Collections.emptyList();
	}

	public List<Class<?>> getRootClasses() {
		return rootClasses;
	}

	private List<LuaMethodDebugInformation> getMethods(Class<?> clazz, boolean isMethod) {
		ArrayList<LuaMethodDebugInformation> list = new ArrayList<LuaMethodDebugInformation>();
		LuaClassDebugInformation information = classes.get(clazz);
		for (LuaMethodDebugInformation methodInfo: information.methods.values()) {
			if (methodInfo.isMethod() == isMethod) {
				list.add(methodInfo);
			}
		}
		Collections.sort(list, methodSorter);
		return list;		
	}
	
	public List<LuaMethodDebugInformation> getFunctionsForClass(Class<?> clazz) {
		return getMethods(clazz, false);
	}

	public List<LuaMethodDebugInformation> getMethodsForClass(Class<?> clazz) {
		return getMethods(clazz, true);
	}
}
