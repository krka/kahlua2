package se.krka.kahlua.vm;

public interface Platform {
    void register(LuaState state);
    double pow(double x, double y);
}
