package se.krka.kahlua.annotation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.File;

import se.krka.kahlua.integration.doc.ApiDocumentationExporter;

import java.io.StringWriter;

import se.krka.kahlua.integration.doc.DokuWikiPrinter;

import se.krka.kahlua.integration.expose.LuaJavaClassExposer;

import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.converter.LuaNumberConverter;
import se.krka.kahlua.converter.LuaTableConverter;
import se.krka.kahlua.converter.LuaConversionError;

import se.krka.kahlua.integration.processor.LuaClassDebugInformation;
import se.krka.kahlua.integration.processor.LuaMethodDebugInformation;


import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;

public class AnnotationTest {

	private LuaConverterManager manager;
	private LuaState state;
	private LuaJavaClassExposer factory;

	@Before
	public void setup() {
		manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		LuaTableConverter.install(manager);

		state = new LuaState(System.out);
		factory = new LuaJavaClassExposer(state, manager);
	}

	@Test
	public void testInheritedAnnotation() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);

		{
			InheritedAnnotationClass testObject = new InheritedAnnotationClass();
			state.getEnvironment().rawset("testObject", testObject);
			String testString = "testObject:inheritedMethodWithArgs('hello', 123)";
			LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
			state.call(closure, null);
			assertEquals(testObject.imba, 123);
			assertEquals(testObject.zomg, "hello");
		}

		{
			InheritedAnnotationClass testObject = new InheritedAnnotationClass();
			state.getEnvironment().rawset("testObject", testObject);
			String testString = "assert(testObject.baseMethodWithArgs); testObject:baseMethodWithArgs(112233, 'world')";
			LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
			state.call(closure, null);
			assertEquals(testObject.foo, 112233);
			assertEquals(testObject.bar, "world");
		}
	}

	@Test
	public void testOverride() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);

		{
			InheritedAnnotationClass testObject = new InheritedAnnotationClass();
			state.getEnvironment().rawset("testObject", testObject);
			String testString = "assert(testObject:baseMethod2() == 'Inherited')";
			LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
			state.call(closure, null);
		}

		{
			BaseAnnotationClass testObject = new BaseAnnotationClass();
			state.getEnvironment().rawset("testObject", testObject);
			String testString = "assert(testObject:baseMethod2() == 'Base')";
			LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
			state.call(closure, null);
		}


	}

	@Test
	public void testBadCall() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);

		InheritedAnnotationClass testObject = new InheritedAnnotationClass();
		state.getEnvironment().rawset("testObject", testObject);
		String testString = "testObject:inheritedMethodWithArgs('hello', 'world')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		try {
			state.call(closure, null);
			fail();
		} catch (Exception e) {
			assertNotNull(e.getMessage());
			assertEquals("No conversion found from class java.lang.String to class java.lang.Integer at argument #2, imba", e.getMessage());
		}
	}

	@Test
	public void testNotEnoughParameters() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);

		InheritedAnnotationClass testObject = new InheritedAnnotationClass();
		state.getEnvironment().rawset("testObject", testObject);
		String testString = "testObject:inheritedMethodWithArgs('hello')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		try {
			state.call(closure, null);
			fail();
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Expected 2 arguments but got 1. Correct syntax: obj:inheritedMethodWithArgs(zomg, imba)");
		}
	}

	@Test
	public void testGlobalFunctionWrongNumberOfParams() throws IOException {

		InheritedAnnotationClass testObject = new InheritedAnnotationClass();
		factory.exposeGlobalFunctions(testObject);

		String testString = "myGlobalFunction('hello')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		try {
			state.call(closure, null);
			fail();
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Expected 4 arguments but got 1. Correct syntax: myGlobalFunction(s, d, b, i)");
		}
	}

	@Test
	public void testGlobalFunctionOk() throws IOException {

		InheritedAnnotationClass testObject = new InheritedAnnotationClass();
		factory.exposeGlobalFunctions(testObject);

		String testString = "myGlobalFunction('hello', 1, true, 3)";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
		assertEquals(testObject.s, "hello");
		assertEquals(testObject.d, 1.0);
		assertEquals(testObject.b, true);
		assertEquals(testObject.i, 3);
	}

	@Test
	public void testGlobalFunctionReturnValues() throws IOException {

		InheritedAnnotationClass testObject = new InheritedAnnotationClass();
		factory.exposeGlobalFunctions(testObject);

		String testString = "local a, b = myGlobalFunction2(5, 7); assert(a == 5*7, '1st'); assert(b == 5+7, '2nd');";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
		assertEquals(testObject.x, 5);
		assertEquals(testObject.y, 7);
	}

	@Test
	public void testMethodWithMultipleReturnValues() throws IOException {

		InheritedAnnotationClass testObject = new InheritedAnnotationClass();
		factory.exposeClass(InheritedAnnotationClass.class);
		state.getEnvironment().rawset("testObject", testObject);

		String testString = "local a, b = testObject:inheritedMethodWithMultipleReturns(); assert(a == 'Hello', '1st'); assert(b == 'World', '2nd');";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);

		testString = "local a, b = testObject:inheritedMethodWithMultipleReturns2('prefix'); assert(a == 'prefixHello', '1st'); assert(b == 'prefixWorld', '2nd');";
		closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);

	}

	@Test
	public void testUserdataProperties() throws IOException {

		factory.exposeClass(InheritedAnnotationClass.class);

		state.getEnvironment().rawset("testObject1", new InheritedAnnotationClass());
		state.getEnvironment().rawset("testObject2", new InheritedAnnotationClass());

		String testString =
				"withproperties(testObject1);" +
						"withproperties(testObject2);" +
						"testObject1.key1 = 'A';" +
						"testObject1.key2 = 'B';" +
						"testObject1.key3 = 'C';" +
						"testObject2.key1 = '1';" +
						"testObject2.key2 = '2';" +
						"testObject2.key3 = '3';" +
						"assert(testObject1.key == nil, 'test1');" +
						"assert(testObject1.key1 == 'A', 'test2');" +
						"assert(testObject1.key2 == 'B', 'test3');" +
						"assert(testObject1.key3 == 'C', 'test4');" +
						"assert(testObject2.key1 == '1', 'test5');" +
						"assert(testObject2.key2 == '2', 'test6');" +
						"assert(testObject2.key3 == '3', 'test7');" +
						"";

		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
	}

	@Test
	public void testDebugData() throws SecurityException, IOException, ClassNotFoundException {
		LuaClassDebugInformation classDebugInfo = LuaClassDebugInformation.getFromStream(InheritedAnnotationClass.class);
		assertNotNull(classDebugInfo);
		assertNotNull(classDebugInfo.methods);
		assertFalse(classDebugInfo.methods.isEmpty());
	}

	@Test
	public void testGetDebugData() throws IOException {

		factory.exposeClass(InheritedAnnotationClass.class);
		factory.exposeClass(LuaMethodDebugInformation.class);
		factory.exposeGlobalFunctions(factory);

		state.getEnvironment().rawset("testObject1", new InheritedAnnotationClass());

		String testString = "assert(testObject1, '0')" +
				"assert(testObject1.inheritedMethodWithMultipleReturns2, '1');" +
				"d = getDebugInfo(testObject1.inheritedMethodWithMultipleReturns2)" +
				"assert(d ~= nil, '2')" +
				"local name, type, desc = d:getParameter(1)" +
				"assert(name == 'a')" +
				"assert(type == 'String')" +
				"assert(desc == nil)" +
				"assert(nil == d:getParameter(2))" +
				"";


		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);

		testString = "assert(testObject1, '0')" +
				"assert(testObject1.inheritedMethodWithArgs, '1');" +
				"d = getDebugInfo(testObject1.inheritedMethodWithArgs)" +
				"assert(d ~= nil, '2')" +
				"local name, type, desc = d:getParameter(1)" +
				"assert(name == 'zomg', name)" +
				"assert(type == 'String', type)" +
				"assert(desc == nil, desc)" +
				"local name, type, desc = d:getParameter(2)" +
				"assert(name == 'imba', name)" +
				"assert(type == 'int', type)" +
				"assert(desc == nil, desc)" +
				"assert(nil == d:getParameter(3))" +
				"";

		closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
	}

	@Test
	public void testStaticMethod() throws IOException {

		factory.exposeClass(InheritedAnnotationClass.class);

		String testString = "s = staticMethod(); assert(s == 'Hello world');";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
	}

	@Test
	public void testGetExposedClasses() throws LuaConversionError {

		factory.exposeClass(InheritedAnnotationClass.class);
		LuaTable exposedClasses = factory.getExposedClasses();
		assertNotNull(exposedClasses);


		Map exposed = manager.fromLuaToJava(exposedClasses, Map.class);
		assertEquals(exposed.size(), 2);
		assertNotNull(exposed.get(InheritedAnnotationClass.class.getName()));
		assertNotNull(exposed.get(BaseAnnotationClass.class.getName()));
	}

	@Test
	public void testConstructor() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);

		String testString = "s = NewBase(); assert(s:baseMethod2() == 'Base');";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
	}

	@Test
	public void testDokuWikiOutput() {
		factory.exposeClass(InheritedAnnotationClass.class);
		ApiDocumentationExporter exporter = new ApiDocumentationExporter(factory.getClassDebugInformation());

		StringWriter writer = new StringWriter();
		DokuWikiPrinter printer = new DokuWikiPrinter(writer, exporter);
		printer.process();
		String output = writer.getBuffer().toString();
	}

	@Test
	public void testVarargs() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);
		String testString = "foo = NewBase(); local s = foo:withVarargs('.', 'java', 'lang', 'String'); assert(s == 'java.lang.String')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
	}

	@Test
	public void testVarargs2() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);
		String testString = "foo = NewBase(); local s = foo:withVarargs('.'); assert(s == '')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		state.call(closure, null);
	}

	@Test
	public void testVarargsFail() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);
		String testString = "foo = NewBase(); local s = foo:withVarargs('.', {}); assert(s == '')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		try {
			state.call(closure, null);
			fail();
		} catch (Exception e) {
			assertEquals("No conversion found from class se.krka.kahlua.vm.LuaTableImpl to class java.lang.String at argument #2, strings", e.getMessage());
		}
	}

	@Test
	public void testVarargsFail2() throws IOException {
		factory.exposeClass(InheritedAnnotationClass.class);
		String testString = "foo = NewBase(); local s = foo:withVarargs(); assert(s == '')";
		LuaClosure closure = LuaCompiler.loadstring(testString, "src", state.getEnvironment());
		try {
			state.call(closure, null);
			fail();
		} catch (Exception e) {
			assertEquals("Expected 1 arguments but got 0. Correct syntax: obj:withVarargs(joinWith, strings)", e.getMessage());
		}

	}

}
