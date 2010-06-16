package se.krka.kahlua.android;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class KahluaInterpreter extends Activity {
	private final Platform platform;
	private final KahluaTable env;
	private final KahluaConverterManager manager;
	private final LuaJavaClassExposer exposer;
	private final LuaCaller caller;
	private final KahluaThread thread;

	private EditText input;
	private TextView output;
	private Button execute;

	private final StringBuffer buffer = new StringBuffer();

	public KahluaInterpreter() {
		platform = new J2SEPlatform();
		env = platform.newEnvironment();
		manager = new KahluaConverterManager();
		KahluaTable java = platform.newTable();
		env.rawset("Java", java);
		exposer = new LuaJavaClassExposer(manager, platform, env, java);
		exposer.exposeGlobalFunctions(this);
		caller = new LuaCaller(manager);
		thread = new KahluaThread(new PrintStream(new OutputStream() {
			@Override
			public void write(int i) throws IOException {
				buffer.append(Character.toString((char) i));
			}
		}), platform, env);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		input = new EditText(this);
		output = new TextView(this);
		execute = new Button(this);

		execute.setText("Run");
		execute.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				execute();
			}
		});


		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.addView(output);
		layout.addView(input);
		layout.addView(execute);

		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(layout);
		setContentView(scrollView);

	}

	private void execute() {
		final String source = input.getText().toString();
		output.append("> " + source + "\n");

		AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
			@Override
			protected void onPreExecute() {
				execute.setEnabled(false);
				execute.setText("Running...");
				input.getText().clear();
				flush();
			}

			@Override
			protected Void doInBackground(String... strings) {
				flush();
				String source = strings[0];
				try {
					LuaClosure closure = LuaCompiler.loadstring(source, null, env);
					LuaReturn result = caller.protectedCall(thread, closure);
					if (result.isSuccess()) {
						for (Object o : result) {
							buffer.append(KahluaUtil.tostring(o, thread) + "\n");
						}
					} else {
						buffer.append(result.getErrorString() + "\n");
						buffer.append(result.getLuaStackTrace() + "\n");
					}
				} catch (Exception e) {
					buffer.append(e.getMessage() + "\n");
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				flush();
				execute.setText("Run");
				execute.setEnabled(true);
			}

			private void flush() {
				output.append(buffer.toString());
				buffer.setLength(0);
			}
		};

		task.execute(source);
	}

	@LuaMethod(global = true)
	public void sleep(double seconds) {
		try {
			Thread.sleep((long) (seconds * 1000));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}

