package se.krka.kahlua;

public interface KahluaState {
    int call(Object function, int nArguments);

    //Object[] call(Object function, Object arg1, Object arg2, Object arg3);
    //Object[] call(Object function, Object[] args);
}
