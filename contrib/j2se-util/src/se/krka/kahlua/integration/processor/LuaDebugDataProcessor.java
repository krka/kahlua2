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

package se.krka.kahlua.integration.processor;

import se.krka.kahlua.vm.LuaException;

import se.krka.kahlua.integration.expose.ReturnValues;

import se.krka.kahlua.integration.annotations.LuaConstructor;

import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaMethod;





import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;


public class LuaDebugDataProcessor implements Processor, ElementVisitor<Void, LuaMethodDebugInformation> {

	private HashMap<String, LuaClassDebugInformation> classes;
	private Filer filer;

	public Iterable<? extends Completion> getCompletions(Element arg0, AnnotationMirror arg1, ExecutableElement arg2, String arg3) {
		return new HashSet<Completion>();
	}

	public Set<String> getSupportedAnnotationTypes() {
		HashSet<String> hashSet = new HashSet<String>();
		hashSet.add(LuaMethod.class.getName());
		hashSet.add(LuaConstructor.class.getName());
		return hashSet;
	}

	public Set<String> getSupportedOptions() {
		return new HashSet<String>();
	}

	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	public void init(ProcessingEnvironment arg0) {
		filer = arg0.getFiler();
		classes = new HashMap<String, LuaClassDebugInformation>();
	}

	public boolean process(Set<? extends TypeElement> arg0, RoundEnvironment arg1) {
		for (TypeElement t: arg0) {
			Set<? extends Element> set = arg1.getElementsAnnotatedWith(t);
			for (Element element: set) {
				element.accept(this, null);
			}
		}
		
		if (arg1.processingOver()) {
			//prettyPrint();
			try {
				store();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public Void visit(Element arg0) {
		return null;
	}

	public Void visit(Element arg0, LuaMethodDebugInformation arg1) {
		return null;
	}

	public Void visitExecutable(ExecutableElement element, LuaMethodDebugInformation arg1) {
		String className = findClass(element);
		String packageName = findPackage(element);

		LuaClassDebugInformation classDebugInfo = getOrCreate(classes, className, packageName, findSimpleClassName(element));

		String methodName = element.getSimpleName().toString();
		
		LuaMethodDebugInformation methodInfo = new LuaMethodDebugInformation();
		
		// Default values - may be overridden
		methodInfo.methodName = methodName;
		methodInfo.isMethod = true;
		methodInfo.returnType = element.getReturnType().toString();
		
		for (AnnotationMirror mirror: element.getAnnotationMirrors()) {
			if (mirror.getAnnotationType().toString().equals(LuaMethod.class.getName())) {
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
				Set<? extends ExecutableElement> name = values.keySet();
				Iterator<? extends ExecutableElement> iterator = name.iterator();
				while (iterator.hasNext()) {
					ExecutableElement luaMethodAnnotation = iterator.next();
					AnnotationValue value = values.get(luaMethodAnnotation);
					String elementName = luaMethodAnnotation.getSimpleName().toString();
					if (elementName.equals("name")) {
						methodInfo.methodName = value.getValue().toString();
					} else if (elementName.equals("global")) {
						methodInfo.isMethod = value.getValue().toString().equals("false");
					}
				}
			} else if (mirror.getAnnotationType().toString().equals(LuaConstructor.class.getName())) {
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
				Set<? extends ExecutableElement> name = values.keySet();
				Iterator<? extends ExecutableElement> iterator = name.iterator();
				while (iterator.hasNext()) {
					ExecutableElement luaMethodAnnotation = iterator.next();
					AnnotationValue value = values.get(luaMethodAnnotation);
					String elementName = luaMethodAnnotation.getSimpleName().toString();
					if (elementName.equals("name")) {
						methodInfo.methodName = value.getValue().toString();
					}
				}
				methodInfo.isMethod = false;
				methodInfo.returnType = className; 
			} else if (mirror.getAnnotationType().toString().equals(Desc.class.getName())) {
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
				Set<? extends ExecutableElement> name = values.keySet();
				Iterator<? extends ExecutableElement> iterator = name.iterator();
				while (iterator.hasNext()) {
					ExecutableElement luaMethodAnnotation = iterator.next();
					AnnotationValue value = values.get(luaMethodAnnotation);
					methodInfo.returnDescription = value.getValue().toString();
				}
			}
		}
		
		for (VariableElement e: element.getParameters()) {
			e.accept(this, methodInfo);
		}
		
		classDebugInfo.methods.put(methodInfo.methodName, methodInfo);
		
		return null;
	}

	private LuaClassDebugInformation getOrCreate(HashMap<String, LuaClassDebugInformation> classes, String className, String packageName, String simpleClassName) {
		LuaClassDebugInformation value = classes.get(className);
		if (value == null) {
			value = new LuaClassDebugInformation(packageName, simpleClassName);
			classes.put(className, value);
		}
		return value;
	}

	private String findClass(Element arg0) {
		if (arg0.getKind() == ElementKind.CLASS) {
			return arg0.toString();
		}
		return findClass(arg0.getEnclosingElement());
	}

	private String findSimpleClassName(Element arg0) {
		if (arg0.getKind() == ElementKind.CLASS) {
			String simpleName = arg0.getSimpleName().toString();
			if (arg0.getEnclosingElement().getKind() == ElementKind.CLASS) {
				return findSimpleClassName(arg0.getEnclosingElement()) + "_" + simpleName;  
			}
			return simpleName;
		}
		return findSimpleClassName(arg0.getEnclosingElement());
	}

	private String findPackage(Element arg0) {
		if (arg0.getKind() == ElementKind.PACKAGE) {
			return arg0.toString();
		}
		return findPackage(arg0.getEnclosingElement());
	}

	public Void visitPackage(PackageElement arg0, LuaMethodDebugInformation arg1) {
		return null;
	}

	public Void visitType(TypeElement arg0, LuaMethodDebugInformation arg1) {
		return null;
	}

	public Void visitTypeParameter(TypeParameterElement arg0, LuaMethodDebugInformation arg1) {
		return null;
	}

	public Void visitUnknown(Element arg0, LuaMethodDebugInformation arg1) {
		return null;
	}

	public Void visitVariable(VariableElement arg0, LuaMethodDebugInformation arg1) {
		String type = arg0.asType().toString();
		String name = arg0.getSimpleName().toString();
		String desc = null;
		List<? extends AnnotationMirror> mirrors = arg0.getAnnotationMirrors();
		for (AnnotationMirror mirror: mirrors) {
			if (mirror.getAnnotationType().toString().equals(Desc.class.getName())) {
				Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
				Set<? extends ExecutableElement> name2 = values.keySet();
				Iterator<? extends ExecutableElement> iterator = name2.iterator();
				while (iterator.hasNext()) {
					ExecutableElement element2 = iterator.next();
					AnnotationValue value = values.get(element2);
					desc = value.getValue().toString();
				}
			}
		}
		if (type.equals(ReturnValues.class.getName()) && arg1.parameterNames.size() == 0) {
			 arg1.returnDescription = desc;
			 arg1.returnType = "multiple values";
		} else {
			arg1.parameterNames.add(name);
			arg1.parameterTypes.add(type);
			arg1.parameterDescriptions.add(desc);
		}
		return null;
	}

	private void store() throws IOException {
		for (Entry<String, LuaClassDebugInformation> entry: classes.entrySet()) {
			LuaClassDebugInformation classDebugInfo = entry.getValue();
			Element[] elements = null;
			FileObject fileObject = filer.createResource(
					StandardLocation.CLASS_OUTPUT,
					classDebugInfo.getPackageName(),
					classDebugInfo.getSimpleClassName() + ".luadebugdata",
					elements);

			OutputStream stream = fileObject.openOutputStream();
			classDebugInfo.saveToStream(stream);
			stream.close();
		}
	}
}
