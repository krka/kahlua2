package se.krka.kahlua.profiler;

import org.junit.Test;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.luaj.compiler.LuaCompiler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

public class ProfilerTest {
	@Test
	public void simpleTest() throws IOException {
		LuaState state = new LuaState();
		LuaClosure fun = LuaCompiler.loadstring(
				"s='a';for i=1,10 do s=s..s;end;function bar(i)\n" +				// 1
						"s:match('a*b')\n" +			// 2
						"end\n" +					// 3
						"function foo()\n" +		// 4
						"for i = 1, 10 do\n" +	// 5
						"bar(i)\n" +				// 6
						"bar(i)\n" +				// 7
						"bar(i)\n" +				// 8
						"bar(i)\n" +				// 9
						"bar(i)\n" +				// 10
						"bar(i)\n" +				// 11
						"bar(i)\n" +				// 12
						"bar(i)\n" +				// 13
						"end\n" +					// 14
						"end\n" +					// 15
						"foo()\n" +					// 16
						"foo()\n",					// 17
				"test.lua",
				state.getEnvironment());

        // Warmup to let the jvm optimize
        /*
        for (int i = 0; i < 10; i++) {
            state.pcall(fun);
        }
        */

        // Set up the sampler
        BufferedProfiler bufferedProfiler = new BufferedProfiler();
		Sampler sampler = new Sampler(state, 1, bufferedProfiler);

        // Run the sampler and the code
		sampler.start();
		state.pcall(fun);
		sampler.stop();

		PrintWriter writer = new PrintWriter(System.out);

		// Simple output:
		// DebugProfiler debugProfiler = new DebugProfiler(writer);
		// bufferedProfiler.sendTo(debugProfiler);
		
        // Aggregate samples
        AggregatingProfiler profiler = new AggregatingProfiler();
        bufferedProfiler.sendTo(profiler);

        // Generate tree
        StacktraceNode stacktraceNode = profiler.toTree(10, 0, 10);

        // Print the tree on standard output
        stacktraceNode.output(writer);
        writer.flush();
        writer.close();
    }
}
