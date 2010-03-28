package se.krka.kahlua.require;

import java.io.Reader;

public interface LuaSourceProvider {
    Reader getLuaSource(String path);
}
