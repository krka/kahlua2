package se.krka.kahlua.scriptengine;

import java.util.Collections;

import java.util.ArrayList;

import java.util.List;
import javax.script.ScriptEngine;

import javax.script.ScriptEngineFactory;

public class KahluaEngineFactory implements ScriptEngineFactory {
	
	private final List<String> extensions = new ArrayList<String>();
	private final List<String> names = new ArrayList<String>();
	public KahluaEngineFactory() {
		extensions.add("lua");
		extensions.add("lbc");
		names.add("kahlua");
		names.add("lua");
	}

	
	@Override
	public String getEngineName() {
		return "kahlua";
	}

	@Override
	public String getEngineVersion() {
		return "0.1";
	}

	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public String getLanguageName() {
		return "Lua";
	}

	@Override
	public String getLanguageVersion() {
		return "5.1";
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(obj);
		stringBuilder.append(":");
		stringBuilder.append(m);
		stringBuilder.append("(");
		boolean first = true;
		for (String s: args) {
			if (first) {
				first = false;
			}  else {
				stringBuilder.append(", ");				
			}
			stringBuilder.append(s);
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}

	@Override
	public List<String> getMimeTypes() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<String> getNames() {
		return names;
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print(" + toDisplay + ")";
	}

	@Override
	public Object getParameter(String key) {
		return null;
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder builder = new StringBuilder();
		for (String s: statements) {
			builder.append(s).append(";");
		}
		return builder.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new KahluaEngine(this);
	}

}
