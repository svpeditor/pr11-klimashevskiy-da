package ru.edu.pr08;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ClientImporter {
    private final ClientCsvParser parser = new ClientCsvParser();
    private final ClientValidator validator = new ClientValidator();
    private final ClientRepository repository;

    public ClientImporter(ClientRepository r) {
        repository = r;
    }

    // ошибка открытия или чтения файла (IOException) уходит наверх и прерывает весь импорт
    public ImportResult importFile(Path path) throws IOException {
        ImportResult result = new ImportResult();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();   // первая строка - заголовок
            if (header == null) {
                return result;
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    importLine(line, lineNumber, result);
                }
            }
        }
        return result;
    }

    // одна строка: ошибка записывается в результат, цикл в importFile идет дальше
    private void importLine(String line, int lineNumber, ImportResult result) {
        ClientDraft draft;
        try {
            draft = parser.parse(line, lineNumber);
        } catch (ImportLineException e) {
            result.addError(new ImportError(e.getLineNumber(), e.getMessage()));
            return;
        }
        List<String> problems = validator.validate(draft);
        if (!problems.isEmpty()) {
            result.addError(new ImportError(lineNumber, String.join("; ", problems)));
            return;
        }
        // дубликат ИНН: остается первая запись, повтор отклоняется
        if (repository.findByInn(draft.inn()) != null) {
            result.addError(new ImportError(lineNumber, "ИНН " + draft.inn() + " уже импортирован, повтор пропущен"));
            return;
        }
        repository.save(new Client(draft.inn(), draft.name(), draft.email()));
        result.addAccepted();
    }
}
