package se.krka.kahlua;

import se.krka.kahlua.internal.Prototype;

public class Closure implements KahluaFunction {
    private final Prototype prototype;
    private final KahluaTable env;

    public Closure(Prototype prototype, KahluaTable env) {
        this.prototype = prototype;
        this.env = env;
    }

    public int invoke(CallFrame frame) {
        throw new UnsupportedOperationException("NYI");
    }
}
