package se.krka.kahlua.require;

import junit.framework.TestCase;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class RequireTest extends TestCase {

    public void testMultipleRequire() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')\nrequire('/b')");
        provider.addSource("/b", "print('Great success')");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        LuaState state = new LuaState(printStream);
        new Require(provider).install(state);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.TRUE, objects[0]);
        String outputString = new String(byteArrayOutputStream.toByteArray());
        assertEquals("Great success\n", outputString);
    }

    public void testSourceNotFound() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')\nrequire('/b')");
        provider.addSource("/b", "require('/c')");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        LuaState state = new LuaState(printStream);
        new Require(provider).install(state);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Does not exist: /c", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Does not exist: /c", objects[1]);

    }

    public void testSuccess() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "print('Great success')");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        LuaState state = new LuaState(printStream);
        new Require(provider).install(state);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.TRUE, objects[0]);
        String outputString = new String(byteArrayOutputStream.toByteArray());
        assertEquals("Great success\n", outputString);
    }

    public void testRuntimeError() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "error'someerror'");

        LuaState state = new LuaState();
        new Require(provider).install(state);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: someerror", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: someerror", objects[1]);

    }

    public void testCompileError() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "all your base are belong to me");

        LuaState state = new LuaState();
        new Require(provider).install(state);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: [string \"/b\"]:1: '=' expected near `your`", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: [string \"/b\"]:1: '=' expected near `your`", objects[1]);

    }

    public void testCyclic() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "require('/a')");

        LuaState state = new LuaState();
        new Require(provider).install(state);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Circular dependency found for: /a", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Circular dependency found for: /a", objects[1]);
    }
}
