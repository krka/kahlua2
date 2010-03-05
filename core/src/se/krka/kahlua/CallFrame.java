package se.krka.kahlua;

public class CallFrame {
    private KahluaTable environment;

    public int getNumArguments() {
        return 0;
    }

    public Object get(int i) {
        return null;
    }

    public KahluaTable getEnvironment() {
        return environment;
    }

    public int push(Object arg) {
        return 0;
    }

    public int push(Object arg1, Object arg2) {
        return 0;
    }
}
