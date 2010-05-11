package se.krka.kahlua.vm;

import se.krka.kahlua.stdlib.BaseLib;

import java.io.IOException;
import java.io.InputStream;

public class KahluaUtil {
    public static final Object WORKER_THREAD_KEY = new Object();
    public static final String TYPE_NIL = "nil";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_FUNCTION = "function";
    public static final String TYPE_TABLE = "table";
    public static final String TYPE_COROUTINE = "coroutine";
    public static final String TYPE_USERDATA = "userdata";

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
        return ((Double) getArg(callFrame, argc, TYPE_NUMBER, funcname)).doubleValue();
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

    /**
	 *
	 * @param callFrame
	 * @param n
	 * @param type must be "string" or "number" or one of the other built in types. Note that this parameter must be interned!
	 * It's not valid to call it with new String("number").  Use null if you don't care which type or expect
	 * more than one type for this argument.
	 * @param function name of the function that calls this. Only for pretty exceptions.
	 * @return variable with index n on the stack, returned as type "type".
	 */
	public static Object getArg(LuaCallFrame callFrame, int n, String type,
				String function) {
		Object o = callFrame.get(n - 1);
		if (o == null) {
			throw new RuntimeException("bad argument #" + n + "to '" + function +
				"' (" + type + " expected, got no value)");
		}
		// type coercion
		if (type == TYPE_STRING) {
			String res = rawTostring(o);
			if (res != null) {
				return res;
			}
		} else if (type == TYPE_NUMBER) {
			Double d = rawTonumber(o);
			if (d != null) {
				return d;
			}
			throw new RuntimeException("bad argument #" + n + " to '" + function +
			"' (number expected, got string)");
		}
		if (type != null) {
			// type checking
			String isType = type(o);
			if (type != isType) {
				fail("bad argument #" + n + " to '" + function +"' (" + type +
					" expected, got " + isType + ")");
			}
		}
		return o;

	}

    public static Object getOptArg(LuaCallFrame callFrame, int n, String type) {
		// Outside of stack
		if (n - 1 >= callFrame.getTop()) {
			return null;
		}

		Object o = callFrame.get(n-1);
		if (o == null) {
			return null;
		}
		// type coercion
		if (type == TYPE_STRING) {
			return rawTostring(o);
		} else if (type == TYPE_NUMBER) {
			return rawTonumber(o);
		}
		// no type checking, this is optional after all
		return o;
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
}
