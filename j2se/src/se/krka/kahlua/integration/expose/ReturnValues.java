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

package se.krka.kahlua.integration.expose;

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.vm.LuaCallFrame;

public class ReturnValues {
	private final KahluaConverterManager manager;
	private final LuaCallFrame callFrame;
	private int args;
	
	ReturnValues(KahluaConverterManager manager, LuaCallFrame callFrame) {
		this.manager = manager;
		this.callFrame = callFrame;
	}
	
	public ReturnValues push(Object obj) {
        args += callFrame.push(manager.fromJavaToLua(obj));
        return this;
	}
	
	public ReturnValues push(Object... objects) {
		for (Object o: objects) {
			push(o);
		}
		return this;
	}
	
	int getNArguments() {
		return args;
	}
}
