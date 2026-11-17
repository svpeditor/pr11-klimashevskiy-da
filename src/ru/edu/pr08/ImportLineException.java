package ru.edu.pr08;

// ошибка одной строки: непроверяемая, импорт после нее продолжается
public class ImportLineException extends RuntimeException {
    private final int lineNumber;

    public ImportLineException(int lineNumber, String message) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
