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

package se.krka.kahlua.integration.expose;

import org.junit.Test;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.*;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class LuaJavaClassExposerTest {
	@Test
	public void testInherited() throws IOException {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();

		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClass(MyInterface.class);

		LuaClosure closure = LuaCompiler.loadstring("local obj = ...; return obj:foo()", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		LuaCaller caller = new LuaCaller(manager);
		LuaReturn res = caller.protectedCall(t, closure, new MyClass());
		assertEquals(true, res.isSuccess());
		assertEquals("Hello world", res.get(0));


	}

	@Test
	public void testInherited2() throws IOException {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();

		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClass(MyInterface.class);

		env.rawset("obj", new MyClass());
		LuaClosure closure = LuaCompiler.loadstring("return obj:foo()", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		Object res = t.call(closure, null);
		assertEquals("Hello world", res);

	}

	@Test
	public void usingJavaEqualsSuccess() throws IOException {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();

		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClassUsingJavaEquals(EqualsClass.class);

		LuaClosure closure = LuaCompiler.loadstring("local o1, o2 = ...; return o1 == o2", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		LuaCaller caller = new LuaCaller(manager);
		LuaReturn res = caller.protectedCall(t, closure, new EqualsClass(1), new EqualsClass(1));
		assertEquals(true, res.isSuccess());
		assertEquals(true, res.get(0));
	}

	@Test
	public void usingJavaEqualsFail() throws IOException {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();

		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClassUsingJavaEquals(EqualsClass.class);

		LuaClosure closure = LuaCompiler.loadstring("local o1, o2 = ...; return o1 == o2", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		LuaCaller caller = new LuaCaller(manager);
		LuaReturn res = caller.protectedCall(t, closure, new EqualsClass(1), new EqualsClass(2));
		assertEquals(true, res.isSuccess());
		assertEquals(false, res.get(0));
	}

	@Test
	public void usingJavaIdentitySuccess() throws IOException {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();

		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClass(EqualsClass.class);

		LuaClosure closure = LuaCompiler.loadstring("local o1, o2 = ...; return o1 == o2", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		LuaCaller caller = new LuaCaller(manager);
		EqualsClass instance = new EqualsClass(1);
		LuaReturn res = caller.protectedCall(t, closure, instance, instance);
		assertEquals(true, res.isSuccess());
		assertEquals(true, res.get(0));
	}

	@Test
	public void usingJavaIdentityFail() throws IOException {
		Platform platform = J2SEPlatform.getInstance();
		KahluaTable env = platform.newEnvironment();
		KahluaConverterManager manager = new KahluaConverterManager();

		LuaJavaClassExposer exposer = new LuaJavaClassExposer(manager, platform, env);
		exposer.exposeClass(EqualsClass.class);

		LuaClosure closure = LuaCompiler.loadstring("local o1, o2 = ...; return o1 == o2", null, env);
		KahluaThread t = KahluaUtil.getWorkerThread(platform, env);
		LuaCaller caller = new LuaCaller(manager);
		LuaReturn res = caller.protectedCall(t, closure, new EqualsClass(1), new EqualsClass(1));
		assertEquals(true, res.isSuccess());
		assertEquals(false, res.get(0));
	}



	static interface MyInterface {
		@LuaMethod
		String foo();
	}

	static class MyClass implements MyInterface {

		@Override
		public String foo() {
			return "Hello world";
		}
	}

	static class EqualsClass {
		private final int value;

		public EqualsClass(int value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			EqualsClass that = (EqualsClass) o;

			if (value != that.value) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return value;
		}
	}
}
