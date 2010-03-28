/*
Copyright (c) 2007-2008 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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
package se.krka.kahlua.test;

import java.util.Vector;

import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;
import se.krka.kahlua.vm.LuaTableImpl;

public class UserdataArray implements JavaFunction {

	private static final int LENGTH = 0;
	private static final int INDEX = 1;
	private static final int NEWINDEX = 2;
	private static final int NEW = 3;
	private static final int PUSH = 4;

	// NOTE: Vector.class won't work in J2ME - so this is used as a workaround
	private static final Class VECTOR_CLASS = new Vector().getClass();

	private static LuaTable metatable;

	public static synchronized void register(LuaState state) {
		if (metatable == null) {
			metatable = new LuaTableImpl();
			metatable.rawset("__metatable", "restricted");
			metatable.rawset("__len", new UserdataArray(LENGTH));
			metatable.rawset("__index", new UserdataArray(INDEX));
			metatable.rawset("__newindex", new UserdataArray(NEWINDEX));

			metatable.rawset("new", new UserdataArray(NEW));
			metatable.rawset("push", new UserdataArray(PUSH));
		}

		state.setClassMetatable(VECTOR_CLASS, metatable);
		state.getEnvironment().rawset("array", metatable);
	}

	private int index;

	private UserdataArray(int index) {
		this.index = index;
	}

	public int call(LuaCallFrame callFrame, int nArguments) {
		switch (index) {
		case LENGTH: return length(callFrame, nArguments);
		case INDEX: return index(callFrame, nArguments);
		case NEWINDEX: return newindex(callFrame, nArguments);
		case NEW: return newVector(callFrame, nArguments);
		case PUSH: return push(callFrame, nArguments);
		}
		return 0;
	}

	private int push(LuaCallFrame callFrame, int nArguments) {
		BaseLib.luaAssert(nArguments >= 2, "not enough parameters");
		Vector v = (Vector) callFrame.get(0);
		Object value = callFrame.get(1);

		v.addElement(value);
		callFrame.push(v);
		return 1;
	}

	private int newVector(LuaCallFrame callFrame, int nArguments) {
		callFrame.push(new Vector());
		return 1;
	}

	private int newindex(LuaCallFrame callFrame, int nArguments) {
		BaseLib.luaAssert(nArguments >= 3, "not enough parameters");
		Vector v = (Vector) callFrame.get(0);
		Object key = callFrame.get(1);
		Object value = callFrame.get(2);

		v.setElementAt(value, (int) LuaState.fromDouble(key));
		return 0;
	}

	private int index(LuaCallFrame callFrame, int nArguments) {
		BaseLib.luaAssert(nArguments >= 2, "not enough parameters");
		Vector v = (Vector) callFrame.get(0);

		Object key = callFrame.get(1);
		Object res;
		if (key instanceof Double) {
			res = v.elementAt((int) LuaState.fromDouble(key));
		} else {
			res = metatable.rawget(key);
		}
		callFrame.push(res);
		return 1;
	}

	private int length(LuaCallFrame callFrame, int nArguments) {
		BaseLib.luaAssert(nArguments >= 1, "not enough parameters");
		Vector v = (Vector) callFrame.get(0);
		double size = v.size();
		callFrame.push(LuaState.toDouble(size));
		return 1;
	}
}
