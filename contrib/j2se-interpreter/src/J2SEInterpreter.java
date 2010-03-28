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
import java.io.IOException;

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;

public class J2SEInterpreter {
	public static void main(String[] args) {
		LuaState state = new LuaState();
		LuaCompiler.register(state);
		
		while (true) {
			System.out.print("> "); System.out.flush();
			LuaClosure closure = getClosure(state, System.console().readLine());
			if (closure != null) {
				Object[] result = state.pcall(closure);
				if (result[0] == Boolean.TRUE) {
					printResults(result, state);
				} else {
					System.out.println(result[1].toString());
					System.out.println(result[2].toString());
				}
			}			
		}
	}

	private static void printResults(Object[] result, LuaState state) {
		for (int i = 1; i < result.length; i++) {
			if (i > 1) {
				 System.out.print("\t");
			}
			System.out.print(BaseLib.tostring(result[i], state));
		}
		System.out.println();
	}

	private static LuaClosure getClosure(LuaState state, String line) {
		if (line == null) {
			System.out.println();
			System.exit(0);
			return null;
		}
		if (line.length() == 0) {
			return null;
		}
		if (line.charAt(0) == '=') {
			line = "return " + line.substring(1);
		}
		try {
			return LuaCompiler.loadstring(line, "stdin", state.getEnvironment());
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (RuntimeException e) {
			if (e.getMessage().contains("<eof>")) {
				System.out.print(">> "); System.out.flush();
				return getClosure(state, line + "\n" + System.console().readLine());
			}		
			System.out.println(e.getMessage()); System.out.flush();
			return null;
		}
	}
}
