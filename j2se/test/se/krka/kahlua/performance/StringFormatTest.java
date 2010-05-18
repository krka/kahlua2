/*
 Copyright (c) 2010 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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

package se.krka.kahlua.performance;

import org.junit.Test;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringFormatTest {
	static interface Runner {
		String run(String format, Double x, Double y);
		String name();
	}

	static class JavaRunner implements Runner {

		@Override
		public String run(String format, Double x, Double y) {
			return String.format(format, x, y);
		}

		@Override
		public String name() {
			return "Java String.format";
		}
	}

	static class LuaRunner implements Runner {
		private final KahluaThread thread;
		private final LuaClosure closure;

		public LuaRunner(KahluaThread thread, LuaClosure closure) {
			this.thread = thread;
			this.closure = closure;
		}

		@Override
		public String run(String format, Double x, Double y) {
			return (String) thread.call(closure, format, x, y);
		}

		@Override
		public String name() {
			return "Lua string.format";
		}
	}

	@Test
	public void testFormat() throws IOException {
		String format = "Hello %3.2f world %13.2f";
		Double x = 123.0;
		Double y = 456.0;


		Platform platform = new J2SEPlatform();
		KahluaTable env = platform.newEnvironment();
		KahluaThread thread = new KahluaThread(platform, env);
		LuaClosure closure1 = LuaCompiler.loadstring("" +
				"local stringformat = string.format;" +
				"return function(format, x, y)" +
				"return stringformat(format, x, y)" +
				"end", null, env);
		LuaClosure closure = (LuaClosure) thread.call(closure1, null, null, null);

		Runner luaRunner = new LuaRunner(thread, closure);
		Runner javaRunner = new JavaRunner();

		List<Runner> list = new ArrayList<Runner>();
		for (int i = 0; i < 20; i++) {
			list.add(luaRunner);
			list.add(javaRunner);
		}
		Collections.shuffle(list);

		for (Runner runner : list) {
			int count = 0;
			long t1 = System.currentTimeMillis();
			long t2;

			while (true) {
				t2 = System.currentTimeMillis();
				if (t2 - t1 > 1000) {
					break;
				}
				runner.run(format, x, y);
				count++;
			}
			double performance = (double) count / (t2 - t1);
			System.out.println(String.format(
					"%30s %10.2f invocations/ms",
					runner.name(),
					performance));
		}
	}
}
