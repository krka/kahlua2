package se.krka.kahlua.j2se;

import se.krka.kahlua.KahluaTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentKahluaTable implements KahluaTable {
    private final Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();

    @Override
    public void rawset(Object key, Object value) {
        map.put(key, value);
    }

    @Override
    public Object rawget(Object key) {
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }
}
