package se.krka.kahlua.j2se;

import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.stdlib.RandomLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.test.UserdataArray;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.Platform;

import java.util.concurrent.ConcurrentHashMap;

public class J2SEPlatform implements Platform {
    @Override
    public double pow(double x, double y) {
        return Math.pow(x, y);
    }

    @Override
    public KahluaTable newTable() {
        return new KahluaTableImpl(new ConcurrentHashMap<Object, Object>());
    }

    @Override
    public KahluaTable newEnvironment() {
        KahluaTable env = newTable();

        env.rawset("_G", env);
        env.rawset("_VERSION", "Kahlua 2 for J2SE");

        MathLib.register(env, this);
        BaseLib.register(env);
        RandomLib.register(this, env);
        UserdataArray.register(env, this);
        StringLib.register(env, this);
        CoroutineLib.register(env, this);
        OsLib.register(env, this);
        TableLib.register(env, this);


        return env;
    }
}
