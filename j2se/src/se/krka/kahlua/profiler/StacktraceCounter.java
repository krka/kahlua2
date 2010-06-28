package se.krka.kahlua.profiler;

import java.util.*;

/** @exclude */
public class StacktraceCounter {
	private final Map<StacktraceElement, StacktraceCounter> children = new HashMap<StacktraceElement, StacktraceCounter>();
	private long time = 0;

	public void addTime(long time) {
		this.time += time;
	}

	public StacktraceCounter getOrCreateChild(StacktraceElement childElement) {
		StacktraceCounter stacktraceCounter = children.get(childElement);
		if (stacktraceCounter == null) {
			stacktraceCounter = new StacktraceCounter();
			children.put(childElement, stacktraceCounter);
		}
		return stacktraceCounter;
	}

	public long getTime() {
		return time;
	}

    public Map<StacktraceElement, StacktraceCounter> getChildren() {
        return children;
    }
}
