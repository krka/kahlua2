package se.krka.kahlua.internal;

import se.krka.kahlua.KahluaException;

public class KahluaUtil {
    public static void fail(String msg) {
        throw new KahluaException(msg);
    }

    public static void doAssert(boolean condition, String msg) {
        if (!condition) {
            fail(msg);
        }
    }

    public static Double toDouble(double v) {
        return new Double(v);
    }
}
