package se.krka.kahlua.profiler;

import se.krka.kahlua.vm.Prototype;

/** @exclude */
public class LuaStacktraceElement implements StacktraceElement {
	private final int pc;
	private final Prototype prototype;

	public LuaStacktraceElement(int pc, Prototype prototype) {
		this.pc = pc;
		this.prototype = prototype;
	}

	public int getLine() {
		if (pc >= 0 && pc < prototype.lines.length) {
			return prototype.lines[pc];
		}
		return 0;
	}
	
	public String getSource() {
		return prototype.name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LuaStacktraceElement)) return false;

		LuaStacktraceElement that = (LuaStacktraceElement) o;

		if (getLine() != that.getLine()) return false;
		if (!prototype.equals(that.prototype)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = getLine();
		result = 31 * result + prototype.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public String name() {
		return getSource() + ":" + getLine();
	}

	@Override
	public String type() {
		return "lua";
	}
}
