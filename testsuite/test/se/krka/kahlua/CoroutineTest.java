package se.krka.kahlua;

import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
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

public class CoroutineTest {

	@org.junit.Test
	public void testResume() throws IOException {
		Platform platform = new J2SEPlatform();
		KahluaTable env = platform.newEnvironment();
		KahluaThread t = new KahluaThread(platform, env);

		LuaClosure setup = LuaCompiler.loadstring("c = coroutine.create(function() return 1, 2, 3 end)", null, env);
		t.call(setup, null);
		LuaClosure resume = LuaCompiler.loadstring("return coroutine.resume(c)", null, env);
		Object res = t.call(resume, null);
		assertEquals(Boolean.TRUE, res);		
	}

	@org.junit.Test
	public void testResume2() throws IOException {
		Platform platform = new J2SEPlatform();
		KahluaTable env = platform.newEnvironment();
		KahluaThread t = new KahluaThread(platform, env);

		LuaClosure setup = LuaCompiler.loadstring(
				"local cc = coroutine.create\n" +
				"local cy = coroutine.yield\n" +
				"c = cc(function() while true do cy() end end)", null, env);
		t.call(setup, null);
		LuaClosure resume = LuaCompiler.loadstring("return coroutine.resume(c)", null, env);
		for (int i = 0; i < 100; i++) {
			Object res = t.call(resume, null);
			assertEquals(Boolean.TRUE, res);
		}
	}

	@org.junit.Test
	public void testResume3() throws IOException {
		Platform platform = new J2SEPlatform();
		KahluaTable env = platform.newEnvironment();
		KahluaThread t = new KahluaThread(platform, env);

		LuaClosure setup = LuaCompiler.loadstring(
				"local cc = coroutine.create\n" +
				"local cy = coroutine.yield\n" +
				"c = cc(function() while true do cy() end end)", null, env);
		t.call(setup, null);
		LuaClosure resume = LuaCompiler.loadstring("return coroutine.resume(c)", null, env);
		for (int i = 0; i < 100; i++) {
			Object res = t.call(resume, null);
			assertEquals(Boolean.TRUE, res);
		}
	}

}
