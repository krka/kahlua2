package se.krka.kahlua.vm;

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
}
