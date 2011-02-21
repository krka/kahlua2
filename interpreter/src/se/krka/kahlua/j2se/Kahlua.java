package se.krka.kahlua.j2se;

import se.krka.kahlua.Version;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.converter.KahluaEnumConverter;
import se.krka.kahlua.converter.KahluaNumberConverter;
import se.krka.kahlua.converter.KahluaTableConverter;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.j2se.interpreter.InteractiveShell;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Kahlua {
	private static final String STDIN_FILENAME = "stdin";
	private final KahluaConverterManager converterManager = new KahluaConverterManager();
	private final J2SEPlatform platform = new J2SEPlatform();
	private final KahluaTable env = platform.newEnvironment();
	private final LuaCaller caller = new LuaCaller(converterManager);

	public Kahlua() {
	}

	public void execute(String s, String... args) throws IOException {
		execute(new StringReader(s), STDIN_FILENAME, args);
	}

	public void execute(File file, String... args) throws IOException {
		execute(new FileReader(file), file.getName(), args);
	}

	public void execute(Reader reader, String name, String... args) throws IOException {
		LuaClosure closure = LuaCompiler.loadis(reader, name, env);
		LuaReturn result = caller.protectedCall(newThread(), closure, args);
		if (!result.isSuccess()) {
			System.err.println(result.getErrorString());
			System.err.println(result.getLuaStackTrace());
			throw result.getJavaException();
		}
	}


	public static void main(String[] args) throws IOException {
		final Kahlua kahlua = new Kahlua();
		KahluaNumberConverter.install(kahlua.getConverterManager());
		KahluaEnumConverter.install(kahlua.getConverterManager());
		new KahluaTableConverter(kahlua.getPlatform()).install(kahlua.getConverterManager());

		boolean interactive = false;
		boolean printVersion = false;

		List<ExceptionRunnable<IOException>> jobs = new ArrayList<ExceptionRunnable<IOException>>();

		List<String> argsList = Arrays.asList(args);
		final Iterator<String> iterator = argsList.iterator();
		while (iterator.hasNext()) {
			final String arg = iterator.next();
			if (arg.startsWith("-")) {
				if (arg.equals("-e")) {
					if (!iterator.hasNext()) {
						failWithUsage();
					}
					final String stat = iterator.next();
					jobs.add(new ExceptionRunnable() {
						@Override
						public void run() throws IOException {
							kahlua.execute(stat, STDIN_FILENAME);
						}
					});
				} else if (arg.equals("-v")) {
					printVersion = true;
				} else if (arg.equals("-i")) {
					interactive = true;
				} else if (arg.equals("--")) {
					if (iterator.hasNext()) {
						String fileName = iterator.next();
						addFile(kahlua, jobs, iterator, fileName);
					} else {
						interactive = true;
					}
				} else if (arg.equals("-")) {
					addStat(kahlua, jobs, iterator);
				}
			} else {
				addFile(kahlua, jobs, iterator, arg);
			}
		}
		if (jobs.isEmpty() && !interactive && !printVersion) {
			failWithUsage();
		}
		if (printVersion) {
			System.out.println(Version.VERSION);
		}
		for (ExceptionRunnable<IOException> job : jobs) {
			job.run();
		}
		if (interactive) {
			new InteractiveShell(kahlua);
		}
	}

	private static void addStat(final Kahlua kahlua, List<ExceptionRunnable<IOException>> jobs, Iterator<String> iterator) {
		addJob(kahlua, jobs, iterator, STDIN_FILENAME, new InputStreamReader(System.in));
	}

	private static void addJob(final Kahlua kahlua, List<ExceptionRunnable<IOException>> jobs, Iterator<String> iterator, final String filename, final InputStreamReader reader) {
		final String[] luaArgs = toArray(iterator);
		jobs.add(new ExceptionRunnable<IOException>() {
			@Override
			public void run() throws IOException {
				kahlua.execute(reader, filename, luaArgs);
			}
		});
	}

	private static void addFile(final Kahlua kahlua, List<ExceptionRunnable<IOException>> jobs, Iterator<String> iterator, final String arg) throws FileNotFoundException {
		addJob(kahlua, jobs, iterator, arg, new FileReader(arg));
	}

	private static String[] toArray(Iterator<String> iterator) {
		ArrayList<String> list = new ArrayList<String>();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		String[] res = new String[list.size()];
		list.toArray(res);
		return res;
	}

	public static void failWithUsage() {
		System.out.println("usage: kahlua [options] [script [args]].");
		System.out.println("Available options are:");
		System.out.println("  -e stat  execute string 'stat'");
		System.out.println("  -i       enter interactive mode after executing 'script'");
		System.out.println("  -v       show version information");
		System.out.println("  --       stop handling options");
		System.out.println("  -        execute stdin and stop handling options");
		System.exit(0);
	}

	public KahluaConverterManager getConverterManager() {
		return converterManager;
	}

	public Platform getPlatform() {
		return platform;
	}

	public KahluaTable getEnvironment() {
		return env;
	}

	public LuaCaller getCaller() {
		return caller;
	}

	public KahluaThread newThread() {
		return new KahluaThread(platform, env);
	}

	private static interface ExceptionRunnable<T extends Throwable> {
		void run() throws T;
	}
}
