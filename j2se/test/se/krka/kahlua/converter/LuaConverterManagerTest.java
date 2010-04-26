package se.krka.kahlua.converter;

import org.junit.Test;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LuaConverterManagerTest {
    private Platform platform = new J2SEPlatform();
    private KahluaTableConverter kahluaTableConverter = new KahluaTableConverter(platform);

    @Test
	public void testPrimitives1() {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Double result = manager.fromLuaToJava(new Double(1.25), double.class);
		assertEquals(result.getClass(), Double.class);
		assertEquals(result.doubleValue(), 1.25);
	}
	
	@Test
	public void testPrimitives2() {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Long result = manager.fromLuaToJava(new Double(123.45), long.class);
		assertEquals(result.getClass(), Long.class);
		assertEquals(result.longValue(), 123);
		
		long result2 = manager.fromLuaToJava(new Double(123.45), long.class);
		assertEquals(result2, 123);		
	}
	
	@Test
	public void testPrimitives3() {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Object result = manager.fromJavaToLua(123);
		assertEquals(result.getClass(), Double.class);
		assertEquals(((Double) result).doubleValue(), 123.0);
		
		result = manager.fromJavaToLua(new Integer(123));
		assertEquals(result.getClass(), Double.class);
		assertEquals(((Double) result).doubleValue(), 123.0);
		
	}

    @Test
	public void testConversionError() {
		LuaConverterManager manager = new LuaConverterManager();
        Long obj = manager.fromLuaToJava(new Double(123.45), long.class);
        assertEquals(null, obj);
    }

	@Test
	public void testJavaToLua(){
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Object object = manager.fromJavaToLua(123L);
		assertEquals(object, new Double(123));
	}
	
	@Test
	public void testJavaToLuaDefault() {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Object obj = new Object();
		Object obj2 = manager.fromJavaToLua(obj);
		assertSame(obj, obj2);
	}
	
	@Test
	public void testJavaToKahluaTables() {
		LuaConverterManager manager = new LuaConverterManager();
        kahluaTableConverter.install(manager);
		List<String> list = new ArrayList<String>();
		list.add("First");
		list.add("Second");
		list.add("Third");
		
		Object obj = manager.fromJavaToLua(list);
		assertTrue(KahluaTable.class.isAssignableFrom(obj.getClass()));
		KahluaTable t = (KahluaTable) obj;
		assertEquals(t.len(), 3);
		assertEquals(t.rawget(1), "First");
		assertEquals(t.rawget(2), "Second");
		assertEquals(t.rawget(3), "Third");
	}
	
	
	@Test
	public void testJavaToKahluaTablesMap() {
		LuaConverterManager manager = new LuaConverterManager();
        kahluaTableConverter.install(manager);
		Map map = new HashMap();
		map.put("X", 123);
		map.put("Y", "Hello");
		map.put("Z", "World");
		
		Object obj = manager.fromJavaToLua(map);
        assertTrue(KahluaTable.class.isAssignableFrom(obj.getClass()));
		KahluaTable t = (KahluaTable) obj;
		assertEquals(t.len(), 0);
		assertEquals(t.rawget("X"), 123);
		assertEquals(t.rawget("Y"), "Hello");
		assertEquals(t.rawget("Z"), "World");
	}	
	
	@Test
	public void testLuaToJavaTables() {
		LuaConverterManager manager = new LuaConverterManager();
        kahluaTableConverter.install(manager);

		KahluaTable t = platform.newTable();
		t.rawset(1, "First");
		t.rawset(2, "Second");
		t.rawset(3, "Third");
		List list = manager.fromLuaToJava(t, List.class);
		assertEquals(list.size(), 3);
		assertEquals(list.get(0), "First");
		assertEquals(list.get(1), "Second");
		assertEquals(list.get(2), "Third");
	}
	
	@Test
	public void testLuaToJavaTablesMap() {
		LuaConverterManager manager = new LuaConverterManager();
        kahluaTableConverter.install(manager);

		KahluaTable t = platform.newTable();
		t.rawset("X", "First");
		t.rawset("Y", "Second");
		t.rawset(t, "Third");
		Map map = manager.fromLuaToJava(t, Map.class);
		assertEquals(map.get("X"), "First");
		assertEquals(map.get("Y"), "Second");
		assertEquals(map.get(t), "Third");
	}

	@Test
	public void testJavaToKahluaTablesRecursion() {
		LuaConverterManager manager = new LuaConverterManager();
        kahluaTableConverter.install(manager);
		LuaNumberConverter.install(manager);
		
		Map map = new HashMap();
		Map map2 = new HashMap();
		map.put("X", 123);
		map.put("Y", map2);
		map.put("Z", "World");
		
		Object obj = manager.fromJavaToLua(map);
		assertEquals(obj.getClass(), KahluaTableImpl.class);
		KahluaTable t = (KahluaTable) obj;
		assertEquals(t.len(), 0);
		assertEquals(t.rawget("X"), 123.0);
		assertEquals(t.rawget("Y").getClass(), KahluaTableImpl.class);
		assertEquals(t.rawget("Z"), "World");
	}	
	
	@Test(expected = RuntimeException.class)
	public void testJavaToKahluaTablesRecursionInfinite() {
		LuaConverterManager manager = new LuaConverterManager();
        kahluaTableConverter.install(manager);

		Map map = new HashMap();
		map.put("X", map);
		
		Object obj = manager.fromJavaToLua(map);
	}

	
	@Test
	public void testSubtypeReturn() {
		LuaConverterManager manager = new LuaConverterManager();
		
		Object obj2 = new StringBuilder();
		Object obj = manager.fromLuaToJava(obj2, Object.class);
		assertSame(obj, obj2);
	}	

    static enum DummyEnum {
        FOO, BAR
    }

    @Test
    public void testEnumConversion() {
        LuaConverterManager manager = new LuaConverterManager();
        KahluaEnumConverter.install(manager);

        DummyEnum x = DummyEnum.FOO;
        Object x2 = manager.fromJavaToLua(x);
        assertEquals("FOO", x2);
        DummyEnum x3 = manager.fromLuaToJava(x2, DummyEnum.class);
        assertEquals(DummyEnum.FOO, x3);
    }

}
