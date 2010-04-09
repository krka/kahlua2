package se.krka.kahlua.profiler;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class BufferedProfiler implements Profiler {
    private final List<Sample> buffer = new ArrayList<Sample>();

    @Override
    public void getSample(Sample sample) {
        buffer.add(sample);
    }

    public void sendTo(Profiler profiler) {
        for (Sample sample : buffer) {
            profiler.getSample(sample);
        }
    }
}
