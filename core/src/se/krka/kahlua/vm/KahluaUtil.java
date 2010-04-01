package se.krka.kahlua.vm;

import se.krka.kahlua.stdlib.BaseLib;

import java.io.IOException;
import java.io.InputStream;

public class KahluaUtil {
	public static boolean luaEquals(Object a, Object b) {
		if (a == null || b == null) {
			return a == b;
		}
		if (a instanceof Double && b instanceof Double) {
			Double ad = (Double) a;
			Double bd = (Double) b;
			return ad.doubleValue() == bd.doubleValue();
		}
		return a == b;
	}

	public static double fromDouble(Object o) {
		return ((Double) o).doubleValue();
	}

	public static Double toDouble(double d) {
		return new Double(d);
	}

	public static Double toDouble(long d) {
		return toDouble((double) d);
	}

	public static Boolean toBoolean(boolean b) {
		return b ? Boolean.TRUE : Boolean.FALSE;
	}

	public static boolean boolEval(Object o) {
		return (o != null) && (o != Boolean.FALSE);
	}

	public static LuaClosure loadByteCodeFromResource(String name, KahluaTable environment) {
		InputStream stream = environment.getClass().getResourceAsStream(name + ".lbc");
		if (stream == null) {
			return null;
		}
		try {
			return Prototype.loadByteCode(stream, environment);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void luaAssert(boolean b, String msg) {
		if (!b) {
			fail(msg);
		}
	}

	public static void fail(String msg) {
		throw new RuntimeException(msg);
	}

    /**
     * Rounds towards even numbers
     * @param x
     */
    public static double round(double x) {
        if (x < 0) {
            return -round(-x);
        }
        x += 0.5;
        double x2 = Math.floor(x);
        if (x2 == x) {
            return x2 - ((long) x2 & 1);
        }
        return x2;
    }

    /* Thanks rici lake for ipow-implementation */

    /**
     * Calculates base^exponent, for non-negative exponents.
     * 0^0 is defined to be 1
     * @return 1 if exponent is zero or negative
     */
    public static long ipow(long base, int exponent) {
        if (exponent <= 0) {
            return 1;
        }
        long b = 1;
        for (b = (exponent & 1) != 0 ? base : 1, exponent >>= 1; exponent != 0; exponent >>= 1) {
            base *= base;
            if ((exponent & 1) != 0) {
                b *= base;
            }
        }
        return b;
    }

    public static boolean isNegative(double vDouble) {
        return Double.doubleToLongBits(vDouble) < 0;
    }

    public static double getDoubleArg(LuaCallFrame callFrame, int argc, String funcname) {
        return ((Double) BaseLib.getArg(callFrame, argc, BaseLib.TYPE_NUMBER, funcname)).doubleValue();
    }
            
    public static KahluaTable getClassMetatables(KahluaTable env, Platform platform) {
        Object classMeta = env.rawget("__classmetatables");
        if (classMeta == null || !(classMeta instanceof KahluaTable)) {
            classMeta = platform.newTable();
            env.rawset("__classmetatables", classMeta);
        }
        return (KahluaTable) classMeta;
    }
}
