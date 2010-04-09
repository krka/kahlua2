package se.krka.kahlua.require;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;

public class MockProvider implements LuaSourceProvider {
    private final Map<String, String> sources = new HashMap<String, String>();
    public Reader getLuaSource(String path) {
        if (sources.containsKey(path)) {
            return new StringReader(sources.get(path));
        }
        return null;

    }

    public void addSource(String path, String source) {
        sources.put(path, source);
    }
}
