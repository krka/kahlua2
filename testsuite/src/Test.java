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

import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.vm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

public class Test {
	private static KahluaThread getThread(File dir) throws FileNotFoundException, IOException {
        Platform platform = new J2SEPlatform();
        KahluaThread thread = new KahluaThread(System.out, platform, platform.newEnvironment());
		OsLib.register(platform, thread.getEnvironment());
		LuaCompiler.register(thread.getEnvironment());

        thread.getEnvironment().rawset("newobject", new JavaFunction(){
            @Override
            public int call(LuaCallFrame callFrame, int nArguments) {
                return callFrame.push(new Object());
            }
        });

		thread.getEnvironment().rawset("luareturnparam", new JavaFunction() {
			@Override
			public int call(LuaCallFrame callFrame, int nArguments) {
				for (int i = 0; i < nArguments; i++) {
					callFrame.push(callFrame.get(nArguments - i - 1));
				}
				callFrame.push(KahluaUtil.toDouble(nArguments));
				return nArguments + 1;
			}
		});


		//thread = runLua(dir, thread, new File(dir, "stdlib.lbc"));
		File testhelper = new File(dir, "testhelper.lua");
		LuaClosure closure = LuaCompiler.loadis(new FileInputStream(testhelper), testhelper.getName(), thread.getEnvironment());
		thread.call(closure, null);
		return thread;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File dir = new File(args[0]);

		KahluaThread thread = getThread(dir);
		
		Object runTest = thread.getEnvironment().rawget("testCall");
		KahluaUtil.luaAssert(runTest != null, "Could not find testCall");
		Object generateReportClosure = thread.getEnvironment().rawget("generatereport");
		KahluaUtil.luaAssert(generateReportClosure != null, "Could not find generatereport");
		Object mergeTestsClosure = thread.getEnvironment().rawget("mergetests");
		KahluaUtil.luaAssert(mergeTestsClosure != null, "Could not find mergetests");

		File[] children = null;
		for (int i = 1; i < args.length; i++) {
			if (args[i].length() > 0) {
				File f = new File(dir, args[i]);
				if (f.exists() && f.isFile()) {
					if (children == null) {
						children = new File[args.length];
					}
					children[i] =  f;
				} else {
					System.err.println(f + " is not a valid file.");
					System.exit(1);
				}
			}
		}
		if (children == null) {
			children = dir.listFiles();
		}

		KahluaTable testsuites = thread.getPlatform().newTable();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (child != null && !child.getName().contains("testhelper") && child.getName().endsWith(".lua")) {
				final Reader stream = new InputStreamReader(new FileInputStream(child), "UTF-8");
				LuaClosure closure = LuaCompiler.loadis(stream, child.getName(), thread.getEnvironment());
				//LuaClosure closure = LuaPrototype.loadByteCode(new FileInputStream(child), thread.getEnvironment());
				System.out.println("Running " + child + "...");
				verifyCorrectStack(thread);
				Object[] results = thread.pcall(runTest, new Object[] {child.getName(), closure});
				if (results[0] != Boolean.TRUE) {
					Object errorMessage = results[1];
					System.out.println("Crash at " + child + ": " +  errorMessage);
					System.out.println(results[2]);
					Throwable stacktrace = (Throwable) (results[3]);
					if (stacktrace != null) {
						stacktrace.printStackTrace();
					}
					verifyCorrectStack(thread);					
					return;
				} else {
					verifyCorrectStack(thread);
					if (!(results[1] instanceof KahluaTable)) {
						KahluaUtil.fail(("Did not get a table back from " + child + ", got a " + results[1] + " instead."));
					}
					testsuites.rawset(new Double(testsuites.len() + 1.0), results[1]);
				}
			}
		}
		Object[] results = thread.pcall(mergeTestsClosure, new Object[] {"Kahlua", testsuites});
		if (results[0] != Boolean.TRUE) {
			KahluaUtil.fail("" + results[1] + ", " + results[2]);
		}
		Object testParent = results[1];
		
		results = thread.pcall(generateReportClosure, new Object[] {testParent});
		if (results[0] == Boolean.TRUE) {
			Long testsOk = new Long(((Double) results[2]).longValue());
			Long testsFail = new Long(((Double) results[3]).longValue());
			System.out.println(String.format("Test result: %d ok. %d failed.", new Object[] { testsOk, testsFail}));
			System.out.print(results[1]);
			if (testsFail > 0) {
				System.exit(1);
			}
		} else {
			System.out.println("Could not generate test report: " +  results[1]);
			((Throwable) (results[3])).printStackTrace();
			System.out.println(results[2]);
		}
	}

	private static void verifyCorrectStack(KahluaThread thread) {
		KahluaUtil.luaAssert(thread.currentCoroutine.getCallframeTop() == 0, "");
		KahluaUtil.luaAssert(thread.currentCoroutine != null, "coroutine is missing");
		KahluaUtil.luaAssert(thread.currentCoroutine.getThread() != null, "coroutine is missing thread " + thread.currentCoroutine);
		KahluaUtil.luaAssert(thread.currentCoroutine.getThread() == thread, "coroutine has wrong thread");
		KahluaUtil.luaAssert(thread.currentCoroutine.getParent() == null, "coroutine is not top level");
	}
}
