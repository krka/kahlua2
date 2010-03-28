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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.test.UserdataArray;
import se.krka.kahlua.vm.*;

public class Test {
	private static LuaState getState(File dir) throws FileNotFoundException, IOException {

		LuaState state = new LuaState(System.out);
		UserdataArray.register(state);
		OsLib.register(state);
		LuaCompiler.register(state);

        state.getEnvironment().rawset("newobject", new JavaFunction(){
            @Override
            public int call(LuaCallFrame callFrame, int nArguments) {
                return callFrame.push(new Object());
            }
        });

		//state = runLua(dir, state, new File(dir, "stdlib.lbc"));
		File testhelper = new File(dir, "testhelper.lua");
		LuaClosure closure = LuaCompiler.loadis(new FileInputStream(testhelper), testhelper.getName(), state.getEnvironment());
		state.call(closure, null);
		return state;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File dir = new File(args[0]);

		LuaState state = getState(dir);
		
		Object runTest = state.getEnvironment().rawget("testCall");
		BaseLib.luaAssert(runTest != null, "Could not find testCall");
		Object generateReportClosure = state.getEnvironment().rawget("generatereport");
		BaseLib.luaAssert(generateReportClosure != null, "Could not find generatereport");
		Object mergeTestsClosure = state.getEnvironment().rawget("mergetests");
		BaseLib.luaAssert(mergeTestsClosure != null, "Could not find mergetests");

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

		LuaTable testsuites = new LuaTableImpl();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (child != null && !child.getName().contains("testhelper") && child.getName().endsWith(".lua")) {
				LuaClosure closure = LuaCompiler.loadis(new FileInputStream(child), child.getName(), state.getEnvironment());
				//LuaClosure closure = LuaPrototype.loadByteCode(new FileInputStream(child), state.getEnvironment());
				System.out.println("Running " + child + "...");
				Object[] results = state.pcall(runTest, new Object[] {child.getName(), closure});
				if (results[0] != Boolean.TRUE) {
					Object errorMessage = results[1];
					System.out.println("Crash at " + child + ": " +  errorMessage);
					System.out.println(results[2]);
					Throwable stacktrace = (Throwable) (results[3]);
					if (stacktrace != null) {
						stacktrace.printStackTrace();
					}
				} else {
					if (!(results[1] instanceof LuaTable)) {
						BaseLib.fail(("Did not get a table back from " + child + ", got a " + results[1] + " instead."));
					}
					testsuites.rawset(new Double(testsuites.len() + 1.0), results[1]);
				}
			}
		}
		Object[] results = state.pcall(mergeTestsClosure, new Object[] {"Kahlua", testsuites});
		if (results[0] != Boolean.TRUE) {
			BaseLib.fail("" + results[1] + ", " + results[2]);
		}
		Object testParent = results[1];
		
		System.out.println("Generating report...");
		results = state.pcall(generateReportClosure, new Object[] {testParent});
		if (results[0] == Boolean.TRUE) {
			File f = new File("testsuite/testreport.html");
			System.out.println(f.getCanonicalPath());
			FileWriter writer = new FileWriter(f);
			writer.write((String) results[1]);
			writer.close();
			Long testsOk = new Long(((Double) results[2]).longValue());
			Long testsFail = new Long(((Double) results[3]).longValue());
			System.out.println(String.format("Test result: %4d ok. %4d failed.", new Object[] { testsOk, testsFail}));
			System.out.println("Detailed test results can be read at testsuite/testreport.html");
			if (testsFail > 0) {
				System.exit(1);
			}
		} else {
			System.out.println("Could not generate reports: " +  results[1]);
			((Throwable) (results[3])).printStackTrace();
			System.out.println(results[2]);
		}
	}
}
