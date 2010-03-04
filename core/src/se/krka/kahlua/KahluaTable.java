package se.krka.kahlua;

public interface KahluaTable {
    void rawset(Object key, Object value);
    Object rawget(Object key);
    int size();
    
}
