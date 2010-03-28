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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LuaClassDebugInformation implements Serializable {
	public static final LuaClassDebugInformation NULL = new LuaClassDebugInformation();

	private static final long serialVersionUID = 7634190901254143200L;

	private final String packageName;
	private final String simpleClassName;

	public Map<String, LuaMethodDebugInformation> methods = new HashMap<String, LuaMethodDebugInformation>();
	
	private LuaClassDebugInformation() {
		packageName = null;
		simpleClassName = null;
	}
	
	public LuaClassDebugInformation(String packageName, String simpleClassName) {
		this.packageName = packageName;
		this.simpleClassName = simpleClassName;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getSimpleClassName() {
		return simpleClassName;
	}
	
	public String getFullClassName() {
		if (packageName == null || packageName.equals("")) {
			return simpleClassName;
		}
		return packageName + "." + simpleClassName;
	}
	
	
	
	public static LuaClassDebugInformation getFromStream(Class<?> clazz) throws IOException, ClassNotFoundException {
		String fileName = getFileName(clazz);
		InputStream stream = clazz.getResourceAsStream(fileName);
		if (stream == null) {
			return null;
		}
		ObjectInputStream objectStream = new ObjectInputStream(stream);
		return (LuaClassDebugInformation) objectStream.readObject();
	}

	private static String getFileName(Class<?> clazz) {
		return "/" + clazz.getPackage().getName().replace('.', '/') + "/" + getSimpleName(clazz) + ".luadebugdata";
	}

	private static String getSimpleName(Class<?> clazz) {
		if (clazz.getEnclosingClass() != null) {
			return getSimpleName(clazz.getEnclosingClass()) + "_" + clazz.getSimpleName();
		}
		return clazz.getSimpleName();
	}

	public void saveToStream(OutputStream stream) throws IOException {
		ObjectOutputStream outputStream = new ObjectOutputStream(stream);
		outputStream.writeObject(this);
	}

	public String getFileName() {
		return getFileName(getClass());
	}
	
	
}
