package se.krka.kahlua.require;

import org.junit.Test;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class RequireTest {

    @Test
    public void testMultipleRequire() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')\nrequire('/b')");
        provider.addSource("/b", "print('Great success')");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);

        Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        KahluaThread state = new KahluaThread(printStream, platform, env);
        new Require(provider).install(env);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.TRUE, objects[0]);
        String outputString = new String(byteArrayOutputStream.toByteArray());
        assertEquals("Great success\n", outputString);
    }

    @Test
    public void testSourceNotFound() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')\nrequire('/b')");
        provider.addSource("/b", "require('/c')");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        KahluaThread state = new KahluaThread(printStream, platform, env);
        new Require(provider).install(env);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Does not exist: /c", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Does not exist: /c", objects[1]);

    }

    @Test
    public void testSuccess() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "print('Great success')");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        KahluaThread state = new KahluaThread(printStream, platform, env);
        new Require(provider).install(env);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.TRUE, objects[0]);
        String outputString = new String(byteArrayOutputStream.toByteArray());
        assertEquals("Great success\n", outputString);
    }

    @Test
    public void testRuntimeError() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "error'someerror'");

        Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        KahluaThread state = new KahluaThread(platform, env);
        new Require(provider).install(env);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: someerror", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: someerror", objects[1]);

    }

    @Test
    public void testCompileError() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "all your base are belong to me");

        Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        KahluaThread state = new KahluaThread(platform, env);
        new Require(provider).install(env);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: [string \"/b\"]:1: '=' expected near `your`", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: [string \"/b\"]:1: '=' expected near `your`", objects[1]);

    }

    @Test
    public void testCyclic() throws IOException {
        MockProvider provider = new MockProvider();
        provider.addSource("/a", "require('/b')");
        provider.addSource("/b", "require('/a')");

        Platform platform = new J2SEPlatform();
        KahluaTable env = platform.newEnvironment();
        KahluaThread state = new KahluaThread(platform, env);
        new Require(provider).install(env);

        LuaClosure luaClosure = LuaCompiler.loadstring("require('/a')", "foo", state.getEnvironment());
        Object[] objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Circular dependency found for: /a", objects[1]);

        objects = state.pcall(luaClosure);
        assertEquals(Boolean.FALSE, objects[0]);
        assertEquals("Error in: /a: Error in: /b: Circular dependency found for: /a", objects[1]);
    }
}
