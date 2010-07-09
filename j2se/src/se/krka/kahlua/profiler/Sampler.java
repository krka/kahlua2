package se.krka.kahlua.profiler;

import se.krka.kahlua.vm.*;

import java.util.TimerTask;
import java.util.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A sampler holds a timer task which periodically
 * inspects the current call frame stack of a Kahlua thread.
 *
 * For each sample taken, the sampler provides it to the profiler.
 */
public class Sampler {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

	private final KahluaThread thread;
	private final Timer timer;
	private final long period;
	private final Profiler profiler;


	/**
	 *
	 * @param thread the Kahlua thread to measure.
	 * @param period the number of milliseconds between each sample.
	 * @param profiler the profiler which should receive the samples.
	 */
    public Sampler(KahluaThread thread, long period, Profiler profiler) {
		this.thread = thread;
		this.period = period;
		this.profiler = profiler;
		timer = new Timer("Kahlua Sampler-" + NEXT_ID.incrementAndGet(), true);
	}

	public void start() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				List<StacktraceElement> list = new ArrayList<StacktraceElement>();
				appendList(list, thread.currentCoroutine);
				profiler.getSample(new Sample(list, period));
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, period);
	}

	private void appendList(List<StacktraceElement> list, Coroutine coroutine) {
        while (coroutine != null) {
			LuaCallFrame[] stack = coroutine.getCallframeStack();
			int top = Math.min(stack.length, coroutine.getCallframeTop());
			for (int i = top - 1; i >= 0; i--) {
				LuaCallFrame frame = stack[i];

                int pc = frame.pc - 1;
                LuaClosure closure = frame.closure;
				JavaFunction javaFunction = frame.javaFunction;
                if (closure != null) {
                    list.add(new LuaStacktraceElement(pc, closure.prototype));
                } else if (javaFunction != null) {
					list.add(new JavaStacktraceElement(javaFunction));
				}
            }
            coroutine = coroutine.getParent();
        }
    }

	public void stop() {
		timer.cancel();
	}
}
