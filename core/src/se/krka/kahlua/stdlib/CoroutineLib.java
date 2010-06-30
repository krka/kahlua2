/*
Copyright (c) 2008 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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
package se.krka.kahlua.stdlib;

import se.krka.kahlua.vm.*;

public class CoroutineLib implements JavaFunction {

	private static final int CREATE = 0;
	private static final int RESUME = 1;
	private static final int YIELD = 2;
	private static final int STATUS = 3;
	private static final int RUNNING = 4;

	private static final int NUM_FUNCTIONS = 5;
	
	
	private static final String[] names;
	
	// NOTE: Coroutine.class won't work in J2ME - so this is used as a workaround
	private static final Class COROUTINE_CLASS = new Coroutine().getClass();
	
	static {
		names = new String[NUM_FUNCTIONS];
		names[CREATE] = "create";
		names[RESUME] = "resume";
		names[YIELD] = "yield";
		names[STATUS] = "status";
		names[RUNNING] = "running";
	}

	private final int index;
	private static final CoroutineLib[] functions;

    static {
		functions = new CoroutineLib[NUM_FUNCTIONS];
		for (int i = 0; i < NUM_FUNCTIONS; i++) {
			functions[i] = new CoroutineLib(i);
		}
	}
	
	public String toString() {
		return "coroutine." + names[index];
	}
	
	public CoroutineLib(int index) {
		this.index = index;
	}

	public static void register(Platform platform, KahluaTable env) {
		KahluaTable coroutine = platform.newTable();
		for (int i = 0; i < NUM_FUNCTIONS; i++) {
			coroutine.rawset(names[i], functions[i]);
		}
		
		coroutine.rawset("__index", coroutine);
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, env);
        metatables.rawset(COROUTINE_CLASS, coroutine);
		env.rawset("coroutine", coroutine);
	}
	
	public int call(LuaCallFrame callFrame, int nArguments) {
		switch (index) {
		case CREATE: return create(callFrame, nArguments);
		case YIELD: return yield(callFrame, nArguments);
		case RESUME: return resume(callFrame, nArguments);
		case STATUS: return status(callFrame, nArguments);
		case RUNNING: return running(callFrame, nArguments);
		default:
			// Should never happen
			// throw new Error("Illegal function object");
			return 0;
		}
	}

	private int running(LuaCallFrame callFrame, int nArguments) {
		Coroutine t = callFrame.coroutine;
		
		// same behaviour as in original lua,
		// return nil if it's the root coroutine
		if (t.parent == null) {
			t = null;
		}
		
		callFrame.push(t);
		return 1;
	}

	private int status(LuaCallFrame callFrame, int nArguments) {
		Coroutine t = getCoroutine(callFrame, nArguments);
		
		String status = getStatus(t, callFrame.coroutine);
		callFrame.push(status);
		return 1;
	}

	private String getStatus(Coroutine t, Coroutine caller) {
		if (caller == t) {
			return "running";
		}
		return t.getStatus();
	}

	private int resume(LuaCallFrame callFrame, int nArguments) {
		Coroutine t = getCoroutine(callFrame, nArguments);
		
		String status = getStatus(t, callFrame.coroutine);
		// equals on strings works because they are both constants
		if (!(status == "suspended")) {
			KahluaUtil.fail(("Can not resume coroutine that is in status: " + status));
		}

		Coroutine parent = callFrame.coroutine;
		t.parent = parent;
		
		LuaCallFrame nextCallFrame = t.currentCallFrame();

		// Is this the first time the coroutine is resumed?
		if (nextCallFrame.nArguments == -1) {
			nextCallFrame.setTop(0);
		}

		// Copy arguments
		for (int i = 1; i < nArguments; i++) {
			nextCallFrame.push(callFrame.get(i));
		}
		
		// Is this the first time the coroutine is resumed?
		if (nextCallFrame.nArguments == -1) {
			nextCallFrame.nArguments = nArguments - 1;
			nextCallFrame.init();
		}

		callFrame.coroutine.thread.currentCoroutine = t;
		
		return 0;
	}

	private static int yield(LuaCallFrame callFrame, int nArguments) {
		Coroutine t = callFrame.coroutine;
		Coroutine parent = t.parent;

		KahluaUtil.luaAssert(parent != null, "Can not yield outside of a coroutine");
		
		LuaCallFrame realCallFrame = t.callFrameStack[t.callFrameTop - 2];
		KahluaThread.yieldHelper(realCallFrame, callFrame, nArguments);
		return 0;
	}

    private int create(LuaCallFrame callFrame, int nArguments) {
		LuaClosure c = getFunction(callFrame, nArguments);

		Coroutine coroutine = new Coroutine(callFrame.coroutine.thread, callFrame.coroutine.environment);
		coroutine.pushNewCallFrame(c, null, 0, 0, -1, true, true);
		callFrame.push(coroutine);
		return 1;
	}

	private LuaClosure getFunction(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
		Object o = callFrame.get(0);
		KahluaUtil.luaAssert(o instanceof LuaClosure, "argument 1 must be a lua function");
		LuaClosure c = (LuaClosure) o;
		return c;
	}

	private Coroutine getCoroutine(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
		Object o = callFrame.get(0);
		KahluaUtil.luaAssert(o instanceof Coroutine, "argument 1 must be a coroutine");
		Coroutine t = (Coroutine) o;
		return t;
	}
}
