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

    public static void register(LuaState state) {
        KahluaTable t = new KahluaTableImpl();
        for (int i = 0; i < NUM_FUNCTIONS - 1; i++) {
            t.rawset(names[i], functions[i]);
        }

        t.rawset("__index", t);
        state.setClassMetatable(RANDOM_CLASS, t);
        state.getEnvironment().rawset("newrandom", NEWRANDOM_FUN);
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
        Random random = getRandom(callFrame, nArguments);
        Object o = callFrame.get(1);
        random.setSeed(o.hashCode());
        return 0;
    }

    private int random(LuaCallFrame callFrame, int nArguments) {
        Random random = getRandom(callFrame, nArguments);

        if (nArguments <= 1) {
            return callFrame.push(KahluaUtil.toDouble(random.nextDouble()));
        }

        double tmp = KahluaUtil.getDoubleArg(callFrame, 2, names[RANDOM]);
        int m = (int) tmp;
        int n;
        if (nArguments == 2) {
            n = m;
            m = 1;
        } else {
            tmp = KahluaUtil.getDoubleArg(callFrame, 3, names[RANDOM]);
            n = (int) tmp;
        }
        return callFrame.push(KahluaUtil.toDouble(m + random.nextInt(n - m + 1)));
    }

    private Random getRandom(LuaCallFrame callFrame, int nArguments) {
        Random random = null;
        if (nArguments > 0) {
            Object o = callFrame.get(0);
            if (o instanceof Random) {
                random = (Random) o;
            }
        }
        if (random == null) {
            KahluaUtil.fail("First argument must be an object of type random.");
        }
        return random;
    }

    private int newRandom(LuaCallFrame callFrame) {
        Random random = new Random();
        return callFrame.push(random);
    }
}
