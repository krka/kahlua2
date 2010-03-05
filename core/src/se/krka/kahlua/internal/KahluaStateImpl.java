package se.krka.kahlua.internal;

import se.krka.kahlua.KahluaState;
import se.krka.kahlua.KahluaTable;

public class KahluaStateImpl implements KahluaState {
    private final KahluaTable environment;
    private Coroutine current;

    public KahluaStateImpl(KahluaTable environment) {
        this.environment = environment;
        current = new Coroutine();
    }

    public int call(Object function, int nArguments) {
        throw new UnsupportedOperationException("NYI");
    }

    private void interpreterLoop() {

    }
}
