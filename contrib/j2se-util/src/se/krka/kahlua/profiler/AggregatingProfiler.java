package se.krka.kahlua.profiler;

public class AggregatingProfiler implements Profiler {
	private final StacktraceCounter root = new StacktraceCounter();

	public AggregatingProfiler() {
	}

	public synchronized void getSample(Sample sample) {
		root.addTime(sample.getTime());

		StacktraceCounter counter = root;
		int n = sample.getList().size() - 1;
		while (n >= 0) {
			StacktraceElement childElement = sample.getList().get(n);
			StacktraceCounter childCounter = counter.getOrCreateChild(childElement);

			childCounter.addTime(sample.getTime());

			counter = childCounter;
			n--;
		}
	}

    public StacktraceNode toTree(int maxDepth, double minTimeRatio, int maxChildren) {
        return StacktraceNode.createFrom(root, new FakeStacktraceElement("Root", "root"), maxDepth, minTimeRatio, maxChildren);
    }
}
