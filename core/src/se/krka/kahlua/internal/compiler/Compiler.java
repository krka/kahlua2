package se.krka.kahlua.internal.compiler;

import java.io.InputStreamReader;

import java.io.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import se.krka.kahlua.CallFrame;
import se.krka.kahlua.Closure;
import se.krka.kahlua.KahluaFunction;
import se.krka.kahlua.KahluaTable;
import se.krka.kahlua.internal.KahluaUtil;

public class Compiler implements KahluaFunction {

	private final int index;

	private static final int LOADSTRING = 0;
	private static final int LOADSTREAM = 1;
	private static final String[] names = new String[] {
		"loadstring",
		"loadstream",
	};

	private static final Compiler[] functions = new Compiler[names.length];
	static {
		for (int i = 0; i < names.length; i++) {
			functions[i] = new Compiler(i);
		}
	}

	private Compiler(int index) {
		this.index = index;
	}

	public static void install(KahluaTable environment) {
		for (int i = 0; i < names.length; i++) {
			environment.rawset(names[i], functions[i]);
		}
	}

	public int invoke(CallFrame callFrame) {
		switch (index) {
		case LOADSTRING: return loadstring(callFrame);
		case LOADSTREAM: return loadstream(callFrame);
		}
		return 0;
	}

	private int loadstream(CallFrame callFrame) {
		try {
			KahluaUtil.doAssert(callFrame.getNumArguments() >= 2, "not enough arguments");
			InputStream is = (InputStream) callFrame.get(0);
			KahluaUtil.doAssert(is != null, "No inputstream given");
			String name = (String) callFrame.get(1);
			return callFrame.push(loadis(is, name, callFrame.getEnvironment()));
		} catch (RuntimeException e) {
			return callFrame.push(null, e.getMessage());
		} catch (IOException e) {
			return callFrame.push(null, e.getMessage());
		}
	}

	private int loadstring(CallFrame callFrame) {
		try {
			KahluaUtil.doAssert(callFrame.getNumArguments() >= 1, "not enough arguments");
			String source = (String) callFrame.get(0);
			KahluaUtil.doAssert(source != null, "No source given");
			String name = (String) callFrame.get(1);
			if (name == null) {
				name = "<stdin>";
			}
			return callFrame.push(loadstring(source, name, callFrame.getEnvironment()));
		} catch (RuntimeException e) {
			return callFrame.push(null, e.getMessage());
		} catch (IOException e) {
			return callFrame.push(null, e.getMessage());
		}
	}

	public static Closure loadis(InputStream inputStream, String name, KahluaTable environment) throws IOException {
		return loadis(new InputStreamReader(inputStream), name, environment);
	}

	public static Closure loadis(Reader reader, String name, KahluaTable environment) throws IOException {
		KahluaUtil.doAssert(name != null, "no name given the compilation unit");
		return new Closure(LexState.compile(reader.read(), reader, name), environment);
	}

	public static Closure loadstring(String source, String name, KahluaTable environment) throws IOException {
		return loadis(new ByteArrayInputStream(source.getBytes("UTF-8")), name, environment);
	}
}

