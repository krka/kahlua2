package se.krka.kahlua.scriptengine;

import se.krka.kahlua.vm.LuaClosure;

import java.io.IOException;

import se.krka.kahlua.luaj.compiler.LuaCompiler;

import se.krka.kahlua.vm.LuaState;

import java.io.Reader;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import javax.script.ScriptEngine;

public class KahluaEngine implements ScriptEngine {

	private final LuaState state;
	private final ScriptEngineFactory factory;

	public KahluaEngine(ScriptEngineFactory factory) {
		this.factory = factory;
		state = new LuaState();
	}
	
	@Override
	public Bindings createBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object eval(String script) throws ScriptException {
		try {
			LuaClosure closure = LuaCompiler.loadstring(script, "stdin", state.getEnvironment());
			return state.call(closure, null);
		} catch (IOException e) {
			throw new ScriptException(e);
		} catch (RuntimeException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		try {
			LuaClosure closure = LuaCompiler.loadis(reader, "stdin", state.getEnvironment());
			return state.call(closure, null);
		} catch (IOException e) {
			throw new ScriptException(e);
		} catch (RuntimeException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		return eval(script);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		return eval(reader);
	}

	@Override
	public Object eval(String script, Bindings n) throws ScriptException {
		return eval(script);
	}

	@Override
	public Object eval(Reader reader, Bindings n) throws ScriptException {
		return eval(reader);
	}

	@Override
	public Object get(String key) {
		return state.tableGet(state.getEnvironment(), key);
	}

	@Override
	public void put(String key, Object value) {
		state.tableSet(state.getEnvironment(), key, value);
	}

	@Override
	public Bindings getBindings(int scope) {
		return null;
	}

	@Override
	public ScriptContext getContext() {
		return null;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
	}

	@Override
	public void setContext(ScriptContext context) {
	}

}
