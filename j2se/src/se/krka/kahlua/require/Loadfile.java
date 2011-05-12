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
package se.krka.kahlua.require;

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class Loadfile implements JavaFunction {
    public void install(KahluaTable environment) {
        environment.rawset("loadfile", this);
    }

    private final LuaSourceProvider luaSourceProvider;

    public Loadfile(LuaSourceProvider luaSourceProvider) {
        this.luaSourceProvider = luaSourceProvider;
    }

    public int call(LuaCallFrame callFrame, int nArguments) {
		String path = KahluaUtil.getStringArg(callFrame, 1, "loadfile");
		Reader source = luaSourceProvider.getLuaSource(path);
		if (source == null) {
			callFrame.pushNil();
			callFrame.push("Does not exist: " + path);
			return 2;
		}
		callFrame.setTop(2);
		callFrame.set(0, source);
		callFrame.set(1, path);
		return LuaCompiler.loadstream(callFrame, 2);
    }
}
