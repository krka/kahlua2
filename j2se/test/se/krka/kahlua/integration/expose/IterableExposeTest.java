/*
 Copyright (c) 2011 Kristofer Karlsson <kristofer.karlsson@gmail.com>, Per Malmén <per.malmen@gmail.com>

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

import org.junit.Test;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IterableExposeTest {

	@Test
	public void exposedIterable() throws Exception {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();


		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClass(MyIterableClass.class);
		exposer.exposeGlobalFunctions(new IterableExposer());

		LuaClosure closure = LuaCompiler.loadstring("local obj = ...; local ret = \"\" for v in iter(obj) do ret = ret .. v end return ret", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		LuaCaller caller = new LuaCaller(manager);
		LuaReturn res = caller.protectedCall(t, closure, new MyIterableClass("one", "two", "three", "four"));
		assertTrue(res.isSuccess());
		assertEquals("onetwothreefour", res.get(0));

	}

	private class MyIterableClass implements Iterable<String> {
		private final List<String> args;

		public MyIterableClass(String... args) {
			this.args = Arrays.asList(args);
		}

		@Override
		public Iterator<String> iterator() {
			return args.iterator();
		}
	}
}
