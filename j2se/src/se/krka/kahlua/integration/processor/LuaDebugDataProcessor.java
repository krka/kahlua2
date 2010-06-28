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

import se.krka.kahlua.integration.annotations.LuaConstructor;
import se.krka.kahlua.integration.annotations.LuaMethod;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;


/** @exclude */
public class LuaDebugDataProcessor implements Processor, ElementVisitor<Void, Void> {

	private HashMap<String, ClassParameterInformation> classes;
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
		classes = new HashMap<String, ClassParameterInformation>();
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

	public Void visit(Element arg0, Void arg1) {
		return null;
	}

	public Void visitExecutable(ExecutableElement element, Void arg1) {
		String className = findClass(element);
		String packageName = findPackage(element);

		ClassParameterInformation classParameterInfo = getOrCreate(classes, className, packageName, findSimpleClassName(element));

		String methodName = element.getSimpleName().toString();
		

        String descriptor = DescriptorUtil.getDescriptor(methodName, element.getParameters());

        List<String> parameterInfoList = new ArrayList<String>();
        for (VariableElement variableElement : element.getParameters()) {
            parameterInfoList.add(variableElement.getSimpleName().toString());
        }

        MethodParameterInformation methodInfo = new MethodParameterInformation(parameterInfoList);

		classParameterInfo.methods.put(descriptor, methodInfo);
		
		return null;
	}

	private ClassParameterInformation getOrCreate(HashMap<String, ClassParameterInformation> classes, String className, String packageName, String simpleClassName) {
		ClassParameterInformation value = classes.get(className);
		if (value == null) {
			value = new ClassParameterInformation(packageName, simpleClassName);
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

	public Void visitPackage(PackageElement arg0, Void arg1) {
		return null;
	}

	public Void visitType(TypeElement arg0, Void arg1) {
		return null;
	}

    @Override
    public Void visitVariable(VariableElement e, Void aVoid) {
        return null;

    }

    public Void visitTypeParameter(TypeParameterElement arg0, Void arg1) {
		return null;
	}

	public Void visitUnknown(Element arg0, Void arg1) {
		return null;
	}

	private void store() throws IOException {
		for (Entry<String, ClassParameterInformation> entry: classes.entrySet()) {
			ClassParameterInformation classParameterInfo = entry.getValue();
			Element[] elements = null;
			FileObject fileObject = filer.createResource(
					StandardLocation.CLASS_OUTPUT,
					classParameterInfo.getPackageName(),
					classParameterInfo.getSimpleClassName() + ".luadebugdata",
					elements);

			OutputStream stream = fileObject.openOutputStream();
			classParameterInfo.saveToStream(stream);
			stream.close();
		}
	}
}
