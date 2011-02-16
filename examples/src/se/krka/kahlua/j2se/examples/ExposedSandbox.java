/*
 Copyright (c) 2011 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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

package se.krka.kahlua.j2se.examples;

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;

import java.io.IOException;

public class ExposedSandbox {
	private final KahluaConverterManager converterManager = new KahluaConverterManager();
	private final J2SEPlatform platform = new J2SEPlatform();
	private final KahluaTable env = platform.newEnvironment();
	private final KahluaThread thread = new KahluaThread(platform, env);
	private final LuaCaller caller = new LuaCaller(converterManager);
	private final LuaJavaClassExposer exposer = new LuaJavaClassExposer(converterManager, platform, env);

	public static void main(String[] args) throws IOException {
		new ExposedSandbox().run();
	}

	public void run() throws IOException {
		exposer.exposeClass(Example.class);
		LuaClosure closure = LuaCompiler.loadstring("local arg1 = ...; print('Greetings ' .. arg1:GetName())", "", env);
		caller.protectedCall(thread, closure, new Example());

		exposer.exposeGlobalFunctions(new GlobalObject());
		closure = LuaCompiler.loadstring("print('2^3 = ' .. pow3(2))", "", env);
		caller.protectedCall(thread, closure);
		closure = LuaCompiler.loadstring("local x, y, z = getCoords('me'); print(string.format('I am at coordinates: %f, %f, %f', x, y, z))", "", env);
		caller.protectedCall(thread, closure);

		// The exposer itself holds some interesting functions:
		exposer.exposeGlobalFunctions(exposer);
		closure = LuaCompiler.loadstring("print('Definition:');print(definition(getCoords))", "", env);
		closure = LuaCompiler.loadstring("print('Definition:');print(definition(pow3))", "", env);
		caller.protectedCall(thread, closure);

	}

	private static class Example {
		@LuaMethod(name = "GetName")
		public String getName() {
			return "Mr Example";
		}
	}

	private static class GlobalObject {
		@LuaMethod(name = "pow3", global = true)
		public double pow3(double x) {
			return x * x * x;
		}

		@Desc("This method returns the coordinates of the user with the specified name")
		@LuaMethod(name = "getCoords", global = true)
		public void getCoords(ReturnValues rv, String name) {
			rv.push(1.2, 3.4, 5.6);
		}

	}
}
