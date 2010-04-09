package se.krka.kahlua.profiler;

import se.krka.kahlua.vm.JavaFunction;

public class JavaStacktraceElement implements StacktraceElement {
	private final JavaFunction javaFunction;

	public JavaStacktraceElement(JavaFunction javaFunction) {
		this.javaFunction = javaFunction;
	}

	@Override
	public String name() {
		return javaFunction.toString();
	}

	@Override
	public String type() {
		return "java";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof JavaStacktraceElement)) return false;

		JavaStacktraceElement that = (JavaStacktraceElement) o;

		return javaFunction == that.javaFunction;
	}

	@Override
	public int hashCode() {
		return javaFunction.hashCode();
	}
}
