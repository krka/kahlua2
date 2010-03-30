package se.krka.kahlua.cldc11;

import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.cldc11.KahluaTableImpl;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.Platform;

public class CLDC11Platform implements Platform {
    public void register(LuaState state) {
        MathLib.register(state);
    }

    public double pow(double x, double y) {
        return MathLib.pow(x, y);
    }

    public KahluaTable newTable() {
        return new KahluaTableImpl();
    }
}
