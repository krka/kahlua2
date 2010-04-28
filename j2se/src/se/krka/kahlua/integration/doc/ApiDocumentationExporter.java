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

import se.krka.kahlua.integration.expose.ClassDebugInformation;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.integration.processor.ClassParameterInformation;
import se.krka.kahlua.integration.processor.MethodParameterInformation;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;

public class ApiDocumentationExporter implements ApiInformation {

	private final Map<Class<?>, ClassDebugInformation> classes;
	
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

	private Comparator<MethodDebugInformation> methodSorter = new Comparator<MethodDebugInformation>() {
		@Override
		public int compare(MethodDebugInformation arg0, MethodDebugInformation arg1) {
			return arg0.getLuaName().compareTo(arg1.getLuaName());
		}
	};

	public ApiDocumentationExporter(Map<Class<?>, ClassDebugInformation> classes) {
		this.classes = classes;
		setupHierarchy();
	}
	
	public void setupHierarchy() {
        for (Map.Entry<Class<?>, ClassDebugInformation> entry : classes.entrySet()) {
			Class<?> clazz = entry.getKey();
			Class<?> zuper = clazz.getSuperclass();
			if (classes.get(zuper) != null) {
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

	private List<MethodDebugInformation> getMethods(Class<?> clazz, boolean isMethod) {
		List<MethodDebugInformation> list = new ArrayList<MethodDebugInformation>();
		ClassDebugInformation information = classes.get(clazz);
		for (MethodDebugInformation methodInfo: information.getMethods().values()) {
			if (methodInfo.isMethod() == isMethod) {
				list.add(methodInfo);
			}
		}
		Collections.sort(list, methodSorter);
		return list;		
	}
	
	public List<MethodDebugInformation> getFunctionsForClass(Class<?> clazz) {
		return getMethods(clazz, false);
	}

	public List<MethodDebugInformation> getMethodsForClass(Class<?> clazz) {
		return getMethods(clazz, true);
	}
}
