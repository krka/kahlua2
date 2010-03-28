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

import se.krka.kahlua.integration.expose.ReturnValues;

import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaClass;
import se.krka.kahlua.integration.annotations.LuaMethod;





import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@LuaClass
public class LuaMethodDebugInformation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3059552311721486815L;
	
	public LuaMethodDebugInformation() {
	}
	
	String methodName;
	String methodDescription;
	
	boolean isMethod;
	
	List<String> parameterTypes = new ArrayList<String>();
	List<String> parameterNames = new ArrayList<String>();
	List<String> parameterDescriptions = new ArrayList<String>();
	
	String returnType;
	String returnDescription;
	
	@LuaMethod
	public String getLuaDescription() {
		if (isMethod) {
			return String.format("obj:%s(%s)", methodName, getParameters());			
		} else {
			return String.format("%s(%s)", methodName, getParameters());			
		}
	}

	private String getType(String type) {
		if (type.equals("void")) {
			return "nothing";
		}
		if (type.startsWith("java.lang.")) {
			return type.substring(10);
		}
		return type;
	}

	@LuaMethod
	public int getNumberOfParameters() {
		return parameterNames.size();
	}
	
	@LuaMethod
	public String getParameters() {
		StringBuilder builder = new StringBuilder();
		int n = parameterNames.size();
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(getParameterName(i));
		}
		return builder.toString();
	}
	
	@LuaMethod
	public String getParameterDescription(int i) {
		return parameterDescriptions.get(i);
	}

	@LuaMethod
	public String getParameterName(int i) {
		return parameterNames.get(i);
	}

	@LuaMethod
	public String getParameterType(int i) {
		return getType(parameterTypes.get(i));
	}
	
	@LuaMethod
	public void getParameter(@Desc("name, type, [description]") ReturnValues r, @Desc("1 to n") int index) {
		index = index - 1;
		if (0 <= index && index < parameterNames.size()) {
			r.push(parameterNames.get(index));
			r.push(getParameterType(index));
			r.push(parameterDescriptions.get(index));
		}
	}

	
	@LuaMethod
	public void getReturn(@Desc("type, [description]") ReturnValues r) {
		r.push(getReturnType());
		r.push(returnDescription);
	}

	@LuaMethod
	public String getReturnValueDescription() {
		String s = getReturnType();
		String desc = returnDescription;
		if (desc != null) {
			s += ": " + desc;
		}
		return s;
	}
		
	@LuaMethod
	public String getReturnDescription() {
		return returnDescription;
	}
	
	@LuaMethod
	public String getReturnType() {
		return getType(returnType);
	}
	
	@LuaMethod
	public boolean isMethod() {
		return isMethod;
	}
	
	@LuaMethod
	public String getName() {
		return methodName;
	}	
}
