package se.krka.kahlua.annotation;

import se.krka.kahlua.integration.expose.ReturnValues;

import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaClass;
import se.krka.kahlua.integration.annotations.LuaMethod;





@LuaClass
public class InheritedAnnotationClass extends BaseAnnotationClass {

	public String zomg;
	public int imba;
	public String s;
	public double d;
	public boolean b;
	public int i;
	public int x;
	public int y;

	@LuaMethod
	public void inheritedMethodWithArgs(String zomg, int imba) {
		this.zomg = zomg;
		this.imba = imba;
	}
	
	@LuaMethod
	public void inheritedMethodWithMultipleReturns(ReturnValues r) {
		r.push("Hello");
		r.push("World");
	}
	
	@LuaMethod
	public void inheritedMethodWithMultipleReturns2(ReturnValues r, String a) {
		r.push(a + "Hello");
		r.push(a + "World");
	}
	
	@LuaMethod(global = true)
	public void myGlobalFunction(String s, double d, boolean b, int i) {
		this.s = s;
		this.d = d;
		this.b = b;
		this.i = i;
	}
	
	@LuaMethod(global = true, name = "myGlobalFunction2")
	public void myGlobalFunction2(@Desc("x*y, x+y") ReturnValues r, @Desc("An integer") int x, @Desc("Another integer") int y) {
		this.x = x;
		this.y = y;
		r.push(x * y);
		r.push(x + y);
	}
	
	@LuaMethod
	public @Desc("always 'inherited'") String baseMethod2() {
		return "Inherited";
	}
	
	@LuaMethod(global=true)
	public static String staticMethod() {
		return "Hello world";
	}
	
}
