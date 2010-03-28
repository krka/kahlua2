package se.krka.kahlua.vm;

public interface KahluaThread {

	int call(int nArguments);
	Object call(Object fun, Object arg1, Object arg2, Object arg3);
	Object call(Object fun, Object[] args);

	int pcall(int nArguments);
	Object[] pcall(Object fun, Object[] args);
	Object[] pcall(Object fun);

	KahluaTable getEnvironment();

	void setmetatable(Object o, KahluaTable metatable);
	Object getmetatable(Object o, boolean raw);
}
