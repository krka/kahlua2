package se.krka.kahlua.profiler;

import se.krka.kahlua.vm.*;

import java.util.TimerTask;
import java.util.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Sampler {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

	private final LuaState state;
	private final Timer timer;
	private final long period;
	private final Profiler profiler;


	public Sampler(LuaState state, long period, Profiler profiler) {
		this.state = state;
		this.period = period;
		this.profiler = profiler;
		timer = new Timer("Kahlua Sampler-" + NEXT_ID.incrementAndGet(), true);
	}

	public void start() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				List<StacktraceElement> list = new ArrayList<StacktraceElement>();
				appendList(list, state.currentThread);
				profiler.getSample(new Sample(list, period));
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, period);
	}

	private void appendList(List<StacktraceElement> list, LuaThread thread) {
        while (thread != null) {
            int top = thread.callFrameTop;
            for (int i = top - 1; i >= 0; i--) {
                LuaCallFrame frame = thread.callFrameStack[i];

                int pc = frame.pc - 1;
                LuaClosure closure = frame.closure;
				JavaFunction javaFunction = frame.javaFunction;
                if (closure != null) {
                    list.add(new LuaStacktraceElement(pc, closure.prototype));
                } else if (javaFunction != null) {
					list.add(new JavaStacktraceElement(javaFunction));
				}
            }
            thread = thread.parent;
        }
    }

	public void stop() {
		timer.cancel();
	}
}
