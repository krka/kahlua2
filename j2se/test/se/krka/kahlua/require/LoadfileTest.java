/*
Copyright (c) 2011 Per Malmén <per.malmen@gmail.com>

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

package se.krka.kahlua.require;

import org.junit.Test;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class LoadfileTest {

	@Test
	public void load() throws IOException {
		MockProvider provider = new MockProvider();
		provider.addSource("/a", "t = {}");

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteArrayOutputStream);
		Platform platform = new J2SEPlatform();
		KahluaTable env = platform.newEnvironment();
		KahluaThread state = new KahluaThread(printStream, platform, env);
		new Loadfile(provider).install(env);

		LuaClosure luaClosure = LuaCompiler.loadstring(
				"loadfile('/a')() if(t == nil) then print('a') else print('b') end", "foo", state.getEnvironment());
		Object[] objects = state.pcall(luaClosure);
		assertEquals(Arrays.toString(objects), Boolean.TRUE, objects[0]);
		String outputString = RequireTest.setNewLines(new String(byteArrayOutputStream.toByteArray()));
		assertEquals("b\n", outputString);
	}

}

