package se.krka.kahlua.annotation;

import se.krka.kahlua.integration.annotations.LuaClass;
import se.krka.kahlua.integration.annotations.LuaMethod;

@LuaClass
public class SimpleAnnotatedClass {

	@LuaMethod
	public void doStuff() {
		
	}

	@LuaMethod(name="MWA")
	public void methodWithArgs(int foo, String bar) {
		
	}
}
