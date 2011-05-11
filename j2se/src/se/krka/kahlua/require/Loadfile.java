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
			KahluaUtil.fail("Does not exist: " + path);
		}
		callFrame.setTop(2);
		callFrame.set(0, source);
		callFrame.set(1, path);
		return LuaCompiler.loadstream(callFrame, 2);
    }
}
