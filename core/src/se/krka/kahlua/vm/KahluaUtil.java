package se.krka.kahlua.vm;

import java.io.IOException;
import java.io.InputStream;

public class KahluaUtil {
	/** @exclude */
    private static final Object WORKER_THREAD_KEY = new Object();
	/** @exclude */
    private static final String TYPE_NIL = "nil";
	/** @exclude */
    private static final String TYPE_STRING = "string";
	/** @exclude */
    private static final String TYPE_NUMBER = "number";
	/** @exclude */
    private static final String TYPE_BOOLEAN = "boolean";
	/** @exclude */
    private static final String TYPE_FUNCTION = "function";
	/** @exclude */
    private static final String TYPE_TABLE = "table";
	/** @exclude */
    private static final String TYPE_COROUTINE = "coroutine";
	/** @exclude */
    private static final String TYPE_USERDATA = "userdata";

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

    public static KahluaTable getClassMetatables(Platform platform, KahluaTable env) {
        return getOrCreateTable(platform, env, "__classmetatables");
    }

    public static KahluaThread getWorkerThread(Platform platform, KahluaTable env) {
        Object workerThread = env.rawget(WORKER_THREAD_KEY);
        if (workerThread == null) {
            workerThread = new KahluaThread(platform, env);
            env.rawset(WORKER_THREAD_KEY, workerThread);
        }
        return (KahluaThread) workerThread;
    }

	public static void setWorkerThread(KahluaTable env, KahluaThread thread) {
		env.rawset(WORKER_THREAD_KEY, thread);
	}


    public static KahluaTable getOrCreateTable(Platform platform, KahluaTable env, String name) {
        Object t = env.rawget(name);
        if (t == null || !(t instanceof KahluaTable)) {
            t = platform.newTable();
            env.rawset(name, t);
        }
        return (KahluaTable) t;
    }

    public static void setupLibrary(KahluaTable env, KahluaThread workerThread, String library) {
        LuaClosure closure = loadByteCodeFromResource(library, env);
        if (closure == null) {
            fail("Could not load " + library + ".lbc");
        }
        workerThread.call(closure, null, null, null);
    }

    public static String numberToString(Double num) {
        if (num.isNaN()) {
            return "nan";
        }
        if (num.isInfinite()) {
            if (isNegative(num.doubleValue())) {
                return "-inf";
            }
            return "inf";
        }
        double n = num.doubleValue();
        if (Math.floor(n) == n && Math.abs(n) < 1e14) {
            return String.valueOf(num.longValue());
        }
        return num.toString();
    }

    public static String type(Object o) {
        if (o == null) {
            return TYPE_NIL;
        }
        if (o instanceof String) {
            return TYPE_STRING;
        }
        if (o instanceof Double) {
            return TYPE_NUMBER;
        }
        if (o instanceof Boolean) {
            return TYPE_BOOLEAN;
        }
        if (o instanceof JavaFunction || o instanceof LuaClosure) {
            return TYPE_FUNCTION;
        }
        if (o instanceof KahluaTable) {
            return TYPE_TABLE;
        }
        if (o instanceof Coroutine) {
            return TYPE_COROUTINE;
        }
        return TYPE_USERDATA;
    }

    public static String tostring(Object o, KahluaThread thread) {
        if (o == null) {
            return TYPE_NIL;
        }
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Double) {
            return rawTostring(o);
        }
        if (o instanceof Boolean) {
            return o == Boolean.TRUE ? "true" : "false";
        }
		if (o instanceof LuaClosure) {
			return "closure 0x" + System.identityHashCode(o);
		}
		if (o instanceof JavaFunction) {
			return "function 0x" + System.identityHashCode(o);
		}

		if (thread != null) {
			Object tostringFun = thread.getMetaOp(o, "__tostring");
			if (tostringFun != null) {
				String res = (String) thread.call(tostringFun, o, null, null);
				return res;
			}
		}

		return o.toString();
	}

    public static Double tonumber(String s) {
        return tonumber(s, 10);
    }

    public static Double tonumber(String s, int radix)  {
        if (radix < 2 || radix > 36) {
            throw new RuntimeException("base out of range");
        }

        try {
            if (radix == 10) {
                return Double.valueOf(s);
            } else {
                return toDouble(Integer.parseInt(s, radix));
            }
        } catch (NumberFormatException e) {
            s = s.toLowerCase();
            if (s.endsWith("nan")) {
                return toDouble(Double.NaN);
            }
            if (s.endsWith("inf")) {
                if (s.charAt(0) == '-') {
                    return toDouble(Double.NEGATIVE_INFINITY);
                }
                return toDouble(Double.POSITIVE_INFINITY);
            }
            return null;
        }
    }

    public static String rawTostring(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Double) {
            return numberToString((Double) o);
        }
        return null;
    }

    public static Double rawTonumber(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof String) {
            return tonumber((String) o);
        }
        return null;
    }

	public static String getStringArg(LuaCallFrame callFrame, int n, String function) {
		Object o = getArg(callFrame, n, function);
		String res = rawTostring(o);
		if (res == null) {
			fail(n, function, "string", type(res));
		}
		return res;
	}

	public static String getOptionalStringArg(LuaCallFrame callFrame, int n) {
		Object o = getOptionalArg(callFrame, n);
		return rawTostring(o);
	}

	public static Double getNumberArg(LuaCallFrame callFrame, int n, String function) {
		Object o = getArg(callFrame, n, function);
		Double res = rawTonumber(o);
		if (res == null) {
			fail(n, function, "double", type(res));
		}
		return res;
	}

	public static Double getOptionalNumberArg(LuaCallFrame callFrame, int n) {
		Object o = getOptionalArg(callFrame, n);
		return rawTonumber(o);
	}

	private static void fail(int n, String function, String wantedType, String gotten) {
		throw new RuntimeException("bad argument #" + n + " to '" + function +
				"' (" + wantedType + " expected, got " + gotten + ")");
	}

	public static void assertArgNotNull(Object o, int n, String type, String function) {
		if (o == null) {
			fail(n, function, type, "null");
		}
	}

	public static Object getOptionalArg(LuaCallFrame callFrame, int n) {
		int top = callFrame.getTop();
		int index = n - 1;
		if (index >= top) {
			return null;
		}
		return callFrame.get(n - 1);
	}

	public static Object getArg(LuaCallFrame callFrame, int n, String function) {
		Object res = getOptionalArg(callFrame, n);
		if (res == null) {
			throw new RuntimeException("missing argument #" + n + "to '" + function + "'");
		}
		return res;
	}

    public static int len(KahluaTable kahluaTable, int low, int high) {
        while (low < high) {
            int middle = (high + low + 1) >> 1;
            Object value = kahluaTable.rawget(middle);
            if (value == null) {
                high = middle - 1;
            } else {
                low = middle;
            }
        }
        return low;
    }

	public static double getDoubleArg(LuaCallFrame callFrame, int i, String name) {
		return getNumberArg(callFrame, i, name).doubleValue();
	}
}
