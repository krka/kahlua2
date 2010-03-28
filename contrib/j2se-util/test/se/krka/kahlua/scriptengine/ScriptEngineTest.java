package se.krka.kahlua.scriptengine;

import javax.script.ScriptException;

import javax.script.ScriptEngineFactory;

import org.junit.Test;

import javax.script.ScriptEngine;

import javax.script.ScriptEngineManager;

import static junit.framework.TestCase.*;

public class ScriptEngineTest {
	
	@Test
	public void testScriptEngine() throws ScriptException {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		
		// TODO: figure out the real way to register an engine, so only a jar needs to be supplied.
		scriptEngineManager.registerEngineName("kahlua", new KahluaEngineFactory());
		
		ScriptEngine engine = scriptEngineManager.getEngineByName("kahlua");
		assertNotNull("Kahlua engine is not installed", engine);
		Object eval = engine.eval("return 1*2*3*4*5*6");
		assertEquals(eval, 720.0);
	}
}
