package se.krka.kahlua.profiler;

/** @exclude */
public class FakeStacktraceElement implements StacktraceElement {
	private final String name;
	private final String type;

	public FakeStacktraceElement(String name, String type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String type() {
		return type;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FakeStacktraceElement)) return false;

		FakeStacktraceElement that = (FakeStacktraceElement) o;

		if (!name.equals(that.name)) return false;
		if (!type.equals(that.type)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return name;
	}
}
