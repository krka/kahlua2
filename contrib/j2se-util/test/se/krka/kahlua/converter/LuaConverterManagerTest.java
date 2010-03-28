package se.krka.kahlua.converter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.krka.kahlua.converter.LuaConversionError;
import se.krka.kahlua.converter.LuaConverterManager;
import se.krka.kahlua.converter.LuaNumberConverter;
import se.krka.kahlua.converter.LuaTableConverter;
import se.krka.kahlua.vm.LuaTable;
import se.krka.kahlua.vm.LuaTableImpl;

public class LuaConverterManagerTest {

	@Test
	public void testPrimitives1() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Double result = manager.fromLuaToJava(new Double(1.25), double.class);
		assertEquals(result.getClass(), Double.class);
		assertEquals(result.doubleValue(), 1.25);
	}
	
	@Test
	public void testPrimitives2() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Long result = manager.fromLuaToJava(new Double(123.45), long.class);
		assertEquals(result.getClass(), Long.class);
		assertEquals(result.longValue(), 123);
		
		long result2 = manager.fromLuaToJava(new Double(123.45), long.class);
		assertEquals(result2, 123);		
	}
	
	@Test
	public void testPrimitives3() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Object result = manager.fromJavaToLua(123);
		assertEquals(result.getClass(), Double.class);
		assertEquals(((Double) result).doubleValue(), 123.0);
		
		result = manager.fromJavaToLua(new Integer(123));
		assertEquals(result.getClass(), Double.class);
		assertEquals(((Double) result).doubleValue(), 123.0);
		
	}
	
	@Test(expected = LuaConversionError.class)
	public void testConversionError() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		manager.fromLuaToJava(new Double(123.45), long.class);		
	}

	@Test
	public void testJavaToLua() throws LuaConversionError{
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Object object = manager.fromJavaToLua(123L);
		assertEquals(object, new Double(123));
	}
	
	@Test
	public void testJavaToLuaDefault() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaNumberConverter.install(manager);
		Object obj = new Object();
		Object obj2 = manager.fromJavaToLua(obj);
		assertSame(obj, obj2);
	}
	
	@Test
	public void testJavaToLuaTables() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaTableConverter.install(manager);
		List<String> list = new ArrayList<String>();
		list.add("First");
		list.add("Second");
		list.add("Third");
		
		Object obj = manager.fromJavaToLua(list);
		assertEquals(obj.getClass(), LuaTableImpl.class);
		LuaTable t = (LuaTable) obj;
		assertEquals(t.len(), 3);
		assertEquals(t.rawget(1), "First");
		assertEquals(t.rawget(2), "Second");
		assertEquals(t.rawget(3), "Third");
	}
	
	
	@Test
	public void testJavaToLuaTablesMap() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaTableConverter.install(manager);
		Map map = new HashMap();
		map.put("X", 123);
		map.put("Y", "Hello");
		map.put("Z", "World");
		
		Object obj = manager.fromJavaToLua(map);
		assertEquals(obj.getClass(), LuaTableImpl.class);
		LuaTable t = (LuaTable) obj;
		assertEquals(t.len(), 0);
		assertEquals(t.rawget("X"), 123);
		assertEquals(t.rawget("Y"), "Hello");
		assertEquals(t.rawget("Z"), "World");
	}	
	
	@Test
	public void testLuaToJavaTables() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaTableConverter.install(manager);
		
		LuaTableImpl t = new LuaTableImpl();
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
	public void testLuaToJavaTablesMap() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaTableConverter.install(manager);
		
		LuaTableImpl t = new LuaTableImpl();
		t.rawset("X", "First");
		t.rawset("Y", "Second");
		t.rawset(t, "Third");
		Map map = manager.fromLuaToJava(t, Map.class);
		assertEquals(map.get("X"), "First");
		assertEquals(map.get("Y"), "Second");
		assertEquals(map.get(t), "Third");
	}

	@Test
	public void testJavaToLuaTablesRecursion() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaTableConverter.install(manager);
		LuaNumberConverter.install(manager);
		
		Map map = new HashMap();
		Map map2 = new HashMap();
		map.put("X", 123);
		map.put("Y", map2);
		map.put("Z", "World");
		
		Object obj = manager.fromJavaToLua(map);
		assertEquals(obj.getClass(), LuaTableImpl.class);
		LuaTable t = (LuaTable) obj;
		assertEquals(t.len(), 0);
		assertEquals(t.rawget("X"), 123.0);
		assertEquals(t.rawget("Y").getClass(), LuaTableImpl.class);
		assertEquals(t.rawget("Z"), "World");
	}	
	
	@Test(expected = LuaConversionError.class)
	public void testJavaToLuaTablesRecursionInfinite() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		LuaTableConverter.install(manager);
		
		Map map = new HashMap();
		map.put("X", map);
		
		Object obj = manager.fromJavaToLua(map);
		
	}	

	
	@Test
	public void testSubtypeReturn() throws LuaConversionError {
		LuaConverterManager manager = new LuaConverterManager();
		
		Object obj2 = new StringBuilder();
		Object obj = manager.fromLuaToJava(obj2, Object.class);
		assertSame(obj, obj2);
	}	
	
}
