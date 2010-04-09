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

import java.util.AbstractList;

public abstract class LuaReturn extends AbstractList<Object> {
	protected final Object[] returnValues;

	protected LuaReturn(Object[] returnValues) {
		this.returnValues = returnValues; 
	}

	public abstract boolean isSuccess();

	// valid when success == false, otherwise throws some exception
	public abstract Object getErrorObject();
	public abstract String getErrorString();
	public abstract String getLuaStackTrace();
	public abstract RuntimeException getJavaException();


	// valid when success == true, otherwise throws some exception
	public Object getFirst() {
		return get(0);
	}

	public Object getSecond() {
		return get(1);
	}

	public Object getThird() {
		return get(2);
	}
	
	@Override
	public Object get(int index) {
		int n = size();
		if (index < 0 || index >= n) {
			throw new IndexOutOfBoundsException("The index " + index + " is outside the bounds [" + 0 
					+ ", " + n + ")");
		}
		return returnValues[index + 1];
	}

	@Override
	public int size() {
		return returnValues.length - 1;
	}

	public static LuaReturn createReturn(Object[] returnValues) {
		Boolean success = (Boolean) returnValues[0];
		if(success) {
			return new LuaSuccess(returnValues);
		}
		return new LuaFail(returnValues);
	}	
}
