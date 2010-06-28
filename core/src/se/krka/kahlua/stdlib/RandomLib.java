package se.krka.kahlua.stdlib;

import se.krka.kahlua.vm.*;

import java.util.Random;

public class RandomLib implements JavaFunction {
    private static final Class RANDOM_CLASS = new Random().getClass();

    private static final int RANDOM = 0;
    private static final int RANDOMSEED = 1;
    private static final int NEWRANDOM = 2;

    private static final int NUM_FUNCTIONS = 3;

    private static final String[] names;
    private static final RandomLib[] functions;
    static {
        names = new String[NUM_FUNCTIONS];
        names[RANDOM] = "random";
        names[RANDOMSEED] = "seed";
        names[NEWRANDOM] = "newrandom";
        functions = new RandomLib[NUM_FUNCTIONS];
        for (int i = 0; i < NUM_FUNCTIONS; i++) {
            functions[i] = new RandomLib(i);
        }
    }
    private static final RandomLib NEWRANDOM_FUN = new RandomLib(NEWRANDOM);
    private final int index;

    public RandomLib(int index) {
        this.index = index;
    }

    public static void register(Platform platform, KahluaTable environment) {
        KahluaTable t = platform.newTable();
        for (int i = 0; i < NUM_FUNCTIONS - 1; i++) {
            t.rawset(names[i], functions[i]);
        }

        t.rawset("__index", t);
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, environment);
        metatables.rawset(RANDOM_CLASS, t);
        environment.rawset("newrandom", NEWRANDOM_FUN);
    }

    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (index) {
            case RANDOM: return random(callFrame, nArguments);
            case RANDOMSEED: return randomSeed(callFrame, nArguments);
            case NEWRANDOM: return newRandom(callFrame);
        }
        return 0;
    }

    private int randomSeed(LuaCallFrame callFrame, int nArguments) {
        Random random = getRandom(callFrame, "seed");
        Object o = callFrame.get(1);
        int hashCode = o == null ? 0 : o.hashCode();
        random.setSeed(hashCode);
        return 0;
    }

    private int random(LuaCallFrame callFrame, int nArguments) {
        Random random = getRandom(callFrame, "random");

		Double min = KahluaUtil.getOptionalNumberArg(callFrame, 2);
		Double max = KahluaUtil.getOptionalNumberArg(callFrame, 3);
		if (min == null) {
			return callFrame.push(KahluaUtil.toDouble(random.nextDouble()));
		}
		int m = min.intValue();
		int n;
		if (max == null) {
			n = m;
			m = 1;
		} else {
			n = max.intValue();
		}
        return callFrame.push(KahluaUtil.toDouble(m + random.nextInt(n - m + 1)));
    }

    private Random getRandom(LuaCallFrame callFrame, String name) {
		Object obj = KahluaUtil.getArg(callFrame, 1, name);
		if (!(obj instanceof Random)) {
			KahluaUtil.fail("First argument to " + name + " must be an object of type random.");
		}
		return (Random) obj;
    }

    private int newRandom(LuaCallFrame callFrame) {
		return callFrame.push(new Random());
    }
}
