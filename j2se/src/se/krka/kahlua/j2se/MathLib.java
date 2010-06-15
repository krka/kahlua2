package se.krka.kahlua.j2se;

import se.krka.kahlua.vm.*;

public class MathLib implements JavaFunction {

	private static final int ABS = 0;
	private static final int ACOS = 1;
	private static final int ASIN = 2;
	private static final int ATAN = 3;
	private static final int ATAN2 = 4;
	private static final int CEIL = 5;
	private static final int COS = 6;
	private static final int COSH = 7;
	private static final int DEG = 8;
	private static final int EXP = 9;
	private static final int FLOOR = 10;
	private static final int FMOD = 11;
	private static final int FREXP = 12;
	private static final int LDEXP = 13;
	private static final int LOG = 14;
	private static final int LOG10 = 15;
	private static final int MODF = 16;
	private static final int POW = 17;
	private static final int RAD = 18;
	private static final int SIN = 19;
	private static final int SINH = 20;
	private static final int SQRT = 21;
	private static final int TAN = 22;
	private static final int TANH = 23;

	private static final int NUM_FUNCTIONS = 24;

	private static final String[] names;
	private static final MathLib[] functions;
	static {
		names = new String[NUM_FUNCTIONS];
		names[ABS] = "abs";
		names[ACOS] = "acos";
		names[ASIN] = "asin";
		names[ATAN] = "atan";
		names[ATAN2] = "atan2";
		names[CEIL] = "ceil";
		names[COS] = "cos";
		names[COSH] = "cosh";
		names[DEG] = "deg";
		names[EXP] = "exp";
		names[FLOOR] = "floor";
		names[FMOD] = "fmod";
		names[FREXP] = "frexp";
		names[LDEXP] = "ldexp";
		names[LOG] = "log";
		names[LOG10] = "log10";
		names[MODF] = "modf";
		names[POW] = "pow";
		names[RAD] = "rad";
		names[SIN] = "sin";
		names[SINH] = "sinh";
		names[SQRT] = "sqrt";
		names[TAN] = "tan";
		names[TANH] = "tanh";
		functions = new MathLib[NUM_FUNCTIONS];
		for (int i = 0; i < NUM_FUNCTIONS; i++) {
			functions[i] = new MathLib(i);
		}
	}

	private final int index;
    private static final double LN2_INV = (1 / Math.log(2));

    public MathLib(int index) {
		this.index = index;
	}

    public static void register(Platform platform, KahluaTable env) {
		KahluaTable math = platform.newTable();
		env.rawset("math", math);

		math.rawset("pi", KahluaUtil.toDouble(Math.PI));
		math.rawset("huge", KahluaUtil.toDouble(Double.POSITIVE_INFINITY));

		for (int i = 0; i < NUM_FUNCTIONS; i++) {
			math.rawset(names[i], functions[i]);
		}
	}

	public String toString() {
		return "math." + names[index];
	}

	public int call(LuaCallFrame callFrame, int nArguments) {
		switch (index) {
			case ABS: return abs(callFrame, nArguments);
			case ACOS: return acos(callFrame, nArguments);
			case ASIN: return asin(callFrame, nArguments);
			case ATAN: return atan(callFrame, nArguments);
			case ATAN2: return atan2(callFrame, nArguments);
			case CEIL: return ceil(callFrame, nArguments);
			case COS: return cos(callFrame, nArguments);
			case COSH: return cosh(callFrame, nArguments);
			case DEG: return deg(callFrame, nArguments);
			case EXP: return exp(callFrame, nArguments);
			case FLOOR: return floor(callFrame, nArguments);
			case FMOD: return fmod(callFrame, nArguments);
			case FREXP: return frexp(callFrame, nArguments);
			case LDEXP: return ldexp(callFrame, nArguments);
			case LOG: return log(callFrame, nArguments);
			case LOG10: return log10(callFrame, nArguments);
			case MODF: return modf(callFrame, nArguments);
			case POW: return pow(callFrame, nArguments);
			case RAD: return rad(callFrame, nArguments);
			case SIN: return sin(callFrame, nArguments);
			case SINH: return sinh(callFrame, nArguments);
			case SQRT: return sqrt(callFrame, nArguments);
			case TAN: return tan(callFrame, nArguments);
			case TANH: return tanh(callFrame, nArguments);
			default: return 0;
		}
	}

	// Generic math functions
	private static int abs(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[ABS]);
		callFrame.push(KahluaUtil.toDouble(Math.abs(x)));
		return 1;
	}

	private static int ceil(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[CEIL]);
		callFrame.push(KahluaUtil.toDouble(Math.ceil(x)));
		return 1;
	}

	private static int floor(LuaCallFrame callFrame, int nArguments)  {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[FLOOR]);
		callFrame.push(KahluaUtil.toDouble(Math.floor(x)));
		return 1;
	}

	public static boolean isNegative(double vDouble) {
		return Double.doubleToLongBits(vDouble) < 0;
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

	private static int modf(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[MODF]);

		boolean negate = false;
		if (isNegative(x)) {
			negate = true;
			x = -x;
		}
		double intPart = Math.floor(x);
		double fracPart;
		if (Double.isInfinite(intPart)) {
			fracPart = 0;
		} else {
			fracPart = x - intPart;
		}
		if (negate) {
			intPart = -intPart;
			fracPart = -fracPart;
		}
		callFrame.push(KahluaUtil.toDouble(intPart), KahluaUtil.toDouble(fracPart));
		return 2;
	}

	private static int fmod(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double v1 = KahluaUtil.getDoubleArg(callFrame, 1, names[FMOD]);
		double v2 = KahluaUtil.getDoubleArg(callFrame, 2, names[FMOD]);

		double res;
		if (Double.isInfinite(v1) || Double.isNaN(v1)) {
			res = Double.NaN;
		} else if (Double.isInfinite(v2)) {
			res = v1;
		} else {
			v2 = Math.abs(v2);
			boolean negate = false;
			if (isNegative(v1)) {
				negate = true;
				v1 = -v1;
			}
			res = v1 - Math.floor(v1/v2) * v2;
			if (negate) {
				res = -res;
			}
		}
		callFrame.push(KahluaUtil.toDouble(res));
		return 1;
	}

	// Hyperbolic functions

	private static int cosh(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[COSH]);
		callFrame.push(KahluaUtil.toDouble(Math.cosh(x)));
		return 1;
	}

	private static int sinh(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[SINH]);
		callFrame.push(KahluaUtil.toDouble(Math.sinh(x)));
		return 1;
	}

	private static int tanh(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[TANH]);
		callFrame.push(KahluaUtil.toDouble(Math.tanh(x)));
		return 1;
	}

	// Trig functions
	private static int deg(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[DEG]);
		callFrame.push(KahluaUtil.toDouble(Math.toDegrees(x)));
		return 1;
	}

	private static int rad(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[RAD]);
		callFrame.push(KahluaUtil.toDouble(Math.toRadians(x)));
		return 1;
	}

	private static int acos(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[ACOS]);
		callFrame.push(KahluaUtil.toDouble(Math.acos(x)));
		return 1;
	}

	private static int asin(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[ASIN]);
		callFrame.push(KahluaUtil.toDouble(Math.asin(x)));
		return 1;
	}

	private static int atan(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[ATAN]);
		callFrame.push(KahluaUtil.toDouble(Math.atan(x)));
		return 1;
	}

	private static int atan2(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double y = KahluaUtil.getDoubleArg(callFrame, 1, names[ATAN2]);
		double x = KahluaUtil.getDoubleArg(callFrame, 2, names[ATAN2]);
		callFrame.push(KahluaUtil.toDouble(Math.atan2(y, x)));
		return 1;
	}


	private static int cos(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[COS]);
		callFrame.push(KahluaUtil.toDouble(Math.cos(x)));
		return 1;
	}

	private static int sin(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[SIN]);
		callFrame.push(KahluaUtil.toDouble(Math.sin(x)));
		return 1;
	}

	private static int tan(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[TAN]);
		callFrame.push(KahluaUtil.toDouble(Math.tan(x)));
		return 1;
	}

	// Power functions
	private static int sqrt(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[SQRT]);
		callFrame.push(KahluaUtil.toDouble(Math.sqrt(x)));
		return 1;
	}

	private static int exp(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[EXP]);
		callFrame.push(KahluaUtil.toDouble(Math.exp(x)));
		return 1;
	}

	private static int pow(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[POW]);
		double y = KahluaUtil.getDoubleArg(callFrame, 2, names[POW]);
		callFrame.push(KahluaUtil.toDouble(Math.pow(x, y)));
		return 1;
	}

	private static int log(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[LOG]);
		callFrame.push(KahluaUtil.toDouble(Math.log(x)));
		return 1;
	}

	private static int log10(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[LOG10]);
		callFrame.push(KahluaUtil.toDouble(Math.log10(x)));
		return 1;
	}


	private static int frexp(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame, 1, names[FREXP]);

		double e, m;
		if (Double.isInfinite(x) || Double.isNaN(x)) {
			e = 0;
			m = x;
		} else {
			e = Math.ceil(Math.log(x) * LN2_INV);
			int div = 1 << ((int) e);
			m = x / div;
		}
		callFrame.push(KahluaUtil.toDouble(m), KahluaUtil.toDouble(e));
		return 2;
	}

	private static int ldexp(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double m = KahluaUtil.getDoubleArg(callFrame, 1, names[LDEXP]);
		double dE = KahluaUtil.getDoubleArg(callFrame, 2, names[LDEXP]);

		double ret;
		double tmp = m + dE;
		if (Double.isInfinite(tmp) || Double.isNaN(tmp)) {
			ret = m;
		} else {
			int e = (int) dE;
			ret = m * (1 << e);
		}

		callFrame.push(KahluaUtil.toDouble(ret));
		return 1;
	}
}
