/*
Copyright (c) 2007-2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package se.krka.kahlua.stdlib;

import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.KahluaTableImpl;

public final class MathLib implements JavaFunction {

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

	public MathLib(int index) {
		this.index = index;
	}

	public static void register(LuaState state) {
		KahluaTable math = new KahluaTableImpl();
		state.getEnvironment().rawset("math", math);

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
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[ABS]);
		callFrame.push(KahluaUtil.toDouble(Math.abs(x)));
		return 1;
	}

	private static int ceil(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[CEIL]);
		callFrame.push(KahluaUtil.toDouble(Math.ceil(x)));
		return 1;
	}


	private static int floor(LuaCallFrame callFrame, int nArguments)  {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[FLOOR]);
		callFrame.push(KahluaUtil.toDouble(Math.floor(x)));
		return 1;
	}


    private static int modf(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[MODF]);

		boolean negate = false;
		if (KahluaUtil.isNegative(x)) {
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
		double v1 = KahluaUtil.getDoubleArg(callFrame,1,names[FMOD]);
		double v2 = KahluaUtil.getDoubleArg(callFrame,2,names[FMOD]);

		double res;
		if (Double.isInfinite(v1) || Double.isNaN(v1)) {
			res = Double.NaN;
		} else if (Double.isInfinite(v2)) {
			res = v1;
		} else {
			v2 = Math.abs(v2);
			boolean negate = false;
			if (KahluaUtil.isNegative(v1)) {
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
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[COSH]);

		double exp_x = exp(x);
		double res = (exp_x + 1 / exp_x) * 0.5;

		callFrame.push(KahluaUtil.toDouble(res));
		return 1;
	}

	private static int sinh(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[SINH]);

		double exp_x = exp(x);
		double res = (exp_x - 1 / exp_x) * 0.5;

		callFrame.push(KahluaUtil.toDouble(res));
		return 1;
	}

	private static int tanh(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[TANH]);

		double exp_x = exp(2 * x);
		double res = (exp_x - 1) / (exp_x + 1);

		callFrame.push(KahluaUtil.toDouble(res));
		return 1;
	}

	// Trig functions
	private static int deg(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[DEG]);
		callFrame.push(KahluaUtil.toDouble(Math.toDegrees(x)));
		return 1;
	}

	private static int rad(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[RAD]);
		callFrame.push(KahluaUtil.toDouble(Math.toRadians(x)));
		return 1;
	}

	private static int acos(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[ACOS]);
		callFrame.push(KahluaUtil.toDouble(acos(x)));
		return 1;
	}

	private static int asin(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[ASIN]);
		callFrame.push(KahluaUtil.toDouble(asin(x)));
		return 1;
	}

	private static int atan(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[ATAN]);
		callFrame.push(KahluaUtil.toDouble(atan(x)));
		return 1;
	}

	private static int atan2(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double y = KahluaUtil.getDoubleArg(callFrame,1,names[ATAN2]);
		double x = KahluaUtil.getDoubleArg(callFrame,2,names[ATAN2]);
		callFrame.push(KahluaUtil.toDouble(atan2(y, x)));
		return 1;
	}


	private static int cos(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[COS]);
		callFrame.push(KahluaUtil.toDouble(Math.cos(x)));
		return 1;
	}

	private static int sin(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[SIN]);
		callFrame.push(KahluaUtil.toDouble(Math.sin(x)));
		return 1;
	}

	private static int tan(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[TAN]);
		callFrame.push(KahluaUtil.toDouble(Math.tan(x)));
		return 1;
	}

	// Power functions
	private static int sqrt(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[SQRT]);
		callFrame.push(KahluaUtil.toDouble(Math.sqrt(x)));
		return 1;
	}

	private static int exp(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[EXP]);
		callFrame.push(KahluaUtil.toDouble(exp(x)));
		return 1;
	}

	private static int pow(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[POW]);
		double y = KahluaUtil.getDoubleArg(callFrame,2,names[POW]);
		callFrame.push(KahluaUtil.toDouble(pow(x, y)));
		return 1;
	}

	private static int log(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[LOG]);
		callFrame.push(KahluaUtil.toDouble(ln(x)));
		return 1;
	}

	private static final double LN10_INV = 1 / ln(10);

	private static int log10(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[LOG10]);
		callFrame.push(KahluaUtil.toDouble(ln(x) * LN10_INV));
		return 1;
	}


	private static final double LN2_INV = 1 / ln(2);

	private static int frexp(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
		double x = KahluaUtil.getDoubleArg(callFrame,1,names[FREXP]);

		double e, m;
		if (Double.isInfinite(x) || Double.isNaN(x)) {
			e = 0;
			m = x;
		} else {
			e = Math.ceil(ln(x) * LN2_INV);
			int div = 1 << ((int) e);
			m = x / div;
		}
		callFrame.push(KahluaUtil.toDouble(m), KahluaUtil.toDouble(e));
		return 2;
	}

	private static int ldexp(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
		double m = KahluaUtil.getDoubleArg(callFrame,1,names[LDEXP]);
		double dE = KahluaUtil.getDoubleArg(callFrame,2,names[LDEXP]);

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





	public static final double EPS = 1e-15;

	/*
	 * Simple implementation of the taylor expansion of
	 * exp(x) = 1 + x + x^2/2 + x^3/6 + ...
	 */
	public static double exp(double x) {
		double x_acc = 1;
		double div = 1;

		double res = 0;
		while (Math.abs(x_acc) > EPS) {
			res = res + x_acc;

			x_acc *= x;
			x_acc /= div;
			div++;
		}
		return res;
	}

	/*
	 * Simple implementation of the taylor expansion of
	 * ln(1 - t) = t - t^2/2 -t^3/3 - ... - t^n/n + ...
	 */
	public static double ln(double x) {
		boolean negative = false;

		if (x < 0) {
			return Double.NaN;
		}
		if (x == 0) {
			return Double.NEGATIVE_INFINITY;
		}
		if (Double.isInfinite(x)) {
			return Double.POSITIVE_INFINITY;
		}
		if (x < 1) {
			negative = true;
			x = 1 / x;
		}
		int multiplier = 1;

		// x must be between 0 and 2 - close to 1 means faster taylor expansion
		while (x >= 1.1) {
			multiplier *= 2;
			x = Math.sqrt(x);
		}
		double t = 1 - x;
		double tpow = t;
		int divisor = 1;
		double result = 0;

		double toSubtract;
		while (Math.abs((toSubtract = tpow / divisor)) > EPS) {
			result -= toSubtract;
			tpow *= t;
			divisor++;
		}
		double res = multiplier * result;
		if (negative) {
			return -res;
		}
		return res;
	}

	public static double pow(double base, double exponent) {
		if ((int) exponent == exponent) {
			return ipow(base, (int) exponent);
		} else {
			return fpow(base, exponent);
		}
	}

	private static double fpow(double base, double exponent) {
		if (base < 0) {
			return Double.NaN;
		}
		return exp(exponent * ln(base));
	}

	/* Thanks rici lake for ipow-implementation */
	public static double ipow(double base, int exponent) {
		boolean inverse = false;
		if (KahluaUtil.isNegative(exponent)) {
			exponent = -exponent;
			inverse = true;
		}
		double b = 1;
		for (b = (exponent & 1) != 0 ? base : 1, exponent >>= 1; exponent != 0; exponent >>= 1) {
			base *= base;
			if ((exponent & 1) != 0) {
				b *= base;
			}
		}
		if (inverse) {
			return 1 / b;
		}
		return b;
	}




	// constants
	static final double sq2p1 = 2.414213562373095048802e0;
	static final double sq2m1  = .414213562373095048802e0;
	static final double p4  = .161536412982230228262e2;
	static final double p3  = .26842548195503973794141e3;
	static final double p2  = .11530293515404850115428136e4;
	static final double p1  = .178040631643319697105464587e4;
	static final double p0  = .89678597403663861959987488e3;
	static final double q4  = .5895697050844462222791e2;
	static final double q3  = .536265374031215315104235e3;
	static final double q2  = .16667838148816337184521798e4;
	static final double q1  = .207933497444540981287275926e4;
	static final double q0  = .89678597403663861962481162e3;
	static final double PIO2 = 1.5707963267948966135E0;

	// reduce
	private static double mxatan(double arg)
	{
		double argsq, value;

		argsq = arg*arg;
		value = ((((p4*argsq + p3)*argsq + p2)*argsq + p1)*argsq + p0);
		value = value/(((((argsq + q4)*argsq + q3)*argsq + q2)*argsq + q1)*argsq + q0);
		return value*arg;
	}

	// reduce
	private static double msatan(double arg)
	{
		if(arg < sq2m1) {
			return mxatan(arg);
		}
		if(arg > sq2p1) {
			return PIO2 - mxatan(1/arg);
		}
		return PIO2/2 + mxatan((arg-1)/(arg+1));
	}

	// implementation of atan
	public static double atan(double arg)
	{
		if(arg > 0) {
			return msatan(arg);
		}
		return -msatan(-arg);
	}

	// implementation of atan2
	public static double atan2(double arg1, double arg2)
	{
		// both are 0 or arg1 is +/- inf
		if(arg1+arg2 == arg1) {
			if(arg1 > 0) {
				return PIO2;
			}
			if(arg1 < 0) {
				return -PIO2;
			}
			return 0;
		}
		arg1 = atan(arg1/arg2);
		if(arg2 < 0) {
			if(arg1 <= 0) {
				return arg1 + Math.PI;
			}
			return arg1 - Math.PI;
		}
		return arg1;
	}

	// implementation of asin
	public static double asin(double arg)
	{
		double temp;
		int sign;

		sign = 0;
		if(arg < 0)
		{
			arg = -arg;
			sign++;
		}
		if(arg > 1) {
			return Double.NaN;
		}
		temp = Math.sqrt(1 - arg*arg);
		if(arg > 0.7) {
			temp = PIO2 - atan(temp/arg);
		} else {
			temp = atan(arg/temp);
		}
		if(sign > 0) {
			temp = -temp;
		}
		return temp;
	}

	// implementation of acos
	public static double acos(double arg)
	{
		if(arg > 1 || arg < -1) {
			return Double.NaN;
		}
		return PIO2 - asin(arg);
	}

}
