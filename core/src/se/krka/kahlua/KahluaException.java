package se.krka.kahlua;

public class KahluaException extends RuntimeException {
    public KahluaException(String errorMessage) {
        super(errorMessage);
    }
}
