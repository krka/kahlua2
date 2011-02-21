package se.krka.kahlua.vm;

public interface Platform {

    double pow(double x, double y);

    KahluaTable newTable();

    KahluaTable newEnvironment();

	void setupEnvironment(KahluaTable env);
}
