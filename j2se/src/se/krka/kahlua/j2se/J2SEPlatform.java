package se.krka.kahlua.j2se;

import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.OsLib;
import se.krka.kahlua.stdlib.RandomLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.test.UserdataArray;
import se.krka.kahlua.threading.BlockingKahluaThread;
import se.krka.kahlua.vm.*;

import java.io.IOException;
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

        MathLib.register(this, env);
        BaseLib.register(env);
        RandomLib.register(this, env);
        UserdataArray.register(this, env);
        StringLib.register(this, env);
        CoroutineLib.register(this, env);
        OsLib.register(this, env);
        TableLib.register(this, env);
        LuaCompiler.register(env);

        KahluaThread workerThread = setupWorkerThread(env);
        KahluaUtil.setupLibrary(env, workerThread, "/stdlib");

        try {
            LuaClosure closure = LuaCompiler.loadis(getClass().getResourceAsStream("/serialize.lua"), "serialize.lua", env);
            workerThread.call(closure, null, null, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return env;
    }

    private KahluaThread setupWorkerThread(KahluaTable env) {
        BlockingKahluaThread thread = new BlockingKahluaThread(this, env);
        env.rawset(KahluaUtil.WORKER_THREAD_KEY, thread);
        return thread;
    }

}
