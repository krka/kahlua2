package se.krka.kahlua.j2se;

import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.stdlib.RandomLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.Platform;

import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class J2SEPlatform implements Platform {
    @Override
    public void register(LuaState state) {
        MathLib.register(state, this);
    }

    @Override
    public double pow(double x, double y) {
        return Math.pow(x, y);
    }

    @Override
    public KahluaTable newTable() {
        return new KahluaTableImpl(new ConcurrentHashMap<Object, Object>());
    }

    public KahluaTable newEnvironment() {
        KahluaTable env = newTable();

        env.rawset("_G", env);
        env.rawset("_VERSION", "Kahlua 2 for J2SE");

        BaseLib.register(env);
        
        return env;
    }
}
