/*
 Copyright (c) 2009 Per Malmén <per.malmen@gmail.com>

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


package se.krka.kahlua.integration;

import se.krka.kahlua.stdlib.BaseLib;

public class LuaFail extends LuaReturn {
	LuaFail(Object[] returnValues) {
		super(returnValues);
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public Object getErrorObject() {
		if (returnValues.length >= 2) {
			return returnValues[1];
		}
		return null;
	}

	@Override
	public String getErrorString() {
		if (returnValues.length >= 2 && returnValues[1] != null) {
			return BaseLib.rawTostring(returnValues[1]);
		}
		return "";
	}

	@Override
	public String getLuaStackTrace() {
		if (returnValues.length >= 3 && returnValues[2] instanceof String) {
			return (String) returnValues[2];
		}
		return "";
	}

	@Override
	public RuntimeException getJavaException() {
		if (returnValues.length >= 4 && returnValues[3] instanceof RuntimeException) {
			return (RuntimeException) returnValues[3];
		}
		return null;
	}

	@Override
	public int size() {
		return 0;
	}
}
