package se.krka.kahlua.cldc11;

import se.krka.kahlua.Version;
import se.krka.kahlua.stdlib.*;
import se.krka.kahlua.vm.*;

public class CLDC11Platform implements Platform {
	private static CLDC11Platform INSTANCE = new CLDC11Platform();
	public static CLDC11Platform getInstance() {
		return INSTANCE;
	}

    public double pow(double x, double y) {
        return MathLib.pow(x, y);
    }

    public KahluaTable newTable() {
        return new KahluaTableImpl();
    }

    public KahluaTable newEnvironment() {
        KahluaTable env = newTable();
		setupEnvironment(env);
        return env;
    }

	public void setupEnvironment(KahluaTable env) {
		env.wipe();
		env.rawset("_G", env);
		env.rawset("_VERSION", Version.VERSION + " (CLDC 1.1)");

		BaseLib.register(env);
		MathLib.register(env);
		RandomLib.register(this, env);
		StringLib.register(this, env);
		CoroutineLib.register(this, env);
		TableLib.register(this, env);
		OsLib.register(this, env);

		KahluaThread workerThread = KahluaUtil.getWorkerThread(this, env);
		KahluaUtil.setupLibrary(env, workerThread, "/stdlib");
	}

}
