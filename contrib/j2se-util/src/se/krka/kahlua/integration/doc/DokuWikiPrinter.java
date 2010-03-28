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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import se.krka.kahlua.integration.processor.LuaMethodDebugInformation;

public class DokuWikiPrinter {

	private final ApiInformation information;
	private final PrintWriter writer;

	public DokuWikiPrinter(File output, ApiInformation information) throws IOException {
		this(new FileWriter(output), information);
	}

	public DokuWikiPrinter(Writer writer, ApiInformation information) {
		this.information = information;
		this.writer = new PrintWriter(writer);
	}

	public void process() {
		printClassHierarchy();
		printFunctions();
		writer.close();
	}

	private void printFunctions() {
		writer.println("====== Global functions ======");
		List<Class<?>> classes = information.getAllClasses();
		for (Class<?> clazz: classes) {
			printClassFunctions(clazz);
		}
	}

	private void printClassFunctions(Class<?> clazz) {
		List<LuaMethodDebugInformation> functionsForClass = information.getFunctionsForClass(clazz);
		if (functionsForClass.size() > 0) {
			writer.printf("===== %s ====\n", clazz.getSimpleName());
			writer.printf("In package: %s\n", clazz.getPackage().getName());
			for (LuaMethodDebugInformation methodInfo: functionsForClass) {
				printFunction(methodInfo, "====");				
			}
			writer.printf("\n----\n\n");
		}
	}

	private void printFunction(LuaMethodDebugInformation methodInfo, String heading) {
		writer.printf("%s %s %s\n", heading, methodInfo.getName(), heading);
		writer.printf("<code lua>%s</code>\n", methodInfo.getLuaDescription());

		for (int i = 0; i < methodInfo.getNumberOfParameters(); i++) {
			String name = methodInfo.getParameterName(i);
			String type = methodInfo.getParameterType(i);
			String description = methodInfo.getParameterDescription(i);
			if (description == null) {
				writer.printf("  - **''%s''** ''%s''\n", type, name);
			} else {
				writer.printf("  - **''%s''** ''%s'': %s\n", type, name, description);
			}
		}
		String returnDescription = methodInfo.getReturnDescription();
		if (returnDescription == null) {
			writer.printf("  * returns ''%s''\n", methodInfo.getReturnType());
		} else {
			writer.printf("  * returns ''%s'': %s\n", methodInfo.getReturnType(), returnDescription);
		}
	}

	private void printClassHierarchy() {
		writer.println("====== Class hierarchy ======");
		List<Class<?>> roots = information.getRootClasses();
		for (Class<?> root: roots) {
			printClassHierarchy(root, null);
		}
	}

	private void printClassHierarchy(Class<?> clazz, Class<?> parent) {
		List<Class<?>> children = information.getChildrenForClass(clazz);
		List<LuaMethodDebugInformation> methodsForClass = information.getMethodsForClass(clazz);
		if (children.size() > 0 || methodsForClass.size() > 0 || parent != null) {
			writer.printf("===== %s =====\n", clazz.getSimpleName());
			writer.printf("In package: ''%s''\n", clazz.getPackage().getName());
			if (parent != null) {
				writer.printf("\nSubclass of [[#%s|%s]]\n", parent.getSimpleName(), parent.getSimpleName());
			}
			if (children.size() > 0) {
				writer.printf("\nChildren: ");
				boolean needsComma = false;
				for (Class<?> child: children) {
					if (needsComma) {
						writer.print(", ");
					} else {
						needsComma = true;
					}
					writer.printf("[[#%s|%s]]", child.getSimpleName(), child.getSimpleName());
				}
			}
			printMethods(clazz);
			writer.printf("\n----\n\n");
			for (Class<?> child: children) {
				printClassHierarchy(child, clazz);
			}
		}
	}

	private void printMethods(Class<?> clazz) {
		List<LuaMethodDebugInformation> methodsForClass = information.getMethodsForClass(clazz);
		if (methodsForClass.size() > 0) {
			//writer.printf("==== Methods ====\n");
			for (LuaMethodDebugInformation methodInfo: methodsForClass) {
				printFunction(methodInfo, "====");
			}
		}
	}

}
