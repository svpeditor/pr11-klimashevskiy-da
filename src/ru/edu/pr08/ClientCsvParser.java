package ru.edu.pr08;

public class ClientCsvParser {
    public static final String SEPARATOR = ";";

    // только разбор на поля, правила проверяет ClientValidator
    public ClientDraft parse(String line, int lineNumber) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != 3) {
            throw new ImportLineException(lineNumber, "ожидалось 3 поля через ;, найдено " + parts.length);
        }
        return new ClientDraft(parts[0].strip(), parts[1].strip(), parts[2].strip());
    }
}
