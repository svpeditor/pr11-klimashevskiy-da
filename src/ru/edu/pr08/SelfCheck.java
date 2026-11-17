package ru.edu.pr08;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SelfCheck {
    private static final ClientValidator VALIDATOR = new ClientValidator();
    private static int passed = 0;
    private static int count = 0;

    public static void main(String[] args) throws IOException {
        checkParser();
        checkValidator();
        checkImporter();
        System.out.println("Unit: " + passed + "/" + count + " passed");
        // свои файлы: java -cp out ru.edu.pr08.SelfCheck data/a.csv data/b.csv
        if (args.length == 0) {
            runFile("data/sample.csv");
        }
        for (String a : args) {
            runFile(a);
        }
    }

    private static void checkParser() {
        ClientCsvParser parser = new ClientCsvParser();
        ClientDraft d = parser.parse(" 7701234567 ; Alpha LLC ;office@alpha.ru", 2);
        check("parse fields", d.inn().equals("7701234567") && d.name().equals("Alpha LLC") && d.email().equals("office@alpha.ru"));
        check("parse empty email", parser.parse("7701234567;Alpha LLC;", 2).email().isEmpty());
        check("parse 2 fields", parseErrorLine(parser, "7701234567;Alpha LLC", 7) == 7);
        check("parse 4 fields", parseErrorLine(parser, "7701234567;Alpha;LLC;a@b.ru", 3) == 3);
    }

    private static void checkValidator() {
        // ИНН: ровно 10 или 12 цифр
        check("inn 10 digits", valid("7701234567", "Alpha LLC", "office@alpha.ru"));
        check("inn 12 digits", valid("781234567890", "Beta Inc", "info@beta.com"));
        check("inn 9 digits", !valid("770123456", "Alpha LLC", "office@alpha.ru"));
        check("inn 11 digits", !valid("77012345678", "Alpha LLC", "office@alpha.ru"));
        check("inn 13 digits", !valid("7812345678901", "Alpha LLC", "office@alpha.ru"));
        check("inn letters", !valid("77012345AB", "Alpha LLC", "office@alpha.ru"));
        check("inn empty", !valid("", "Alpha LLC", "office@alpha.ru"));
        // email: один @, непустые части
        check("email short", valid("7701234567", "Alpha LLC", "a@b"));
        check("email without @", !valid("7701234567", "Alpha LLC", "wrongmail"));
        check("email two @", !valid("7701234567", "Alpha LLC", "user@@mail.ru"));
        check("email empty left", !valid("7701234567", "Alpha LLC", "@mail.ru"));
        check("email empty right", !valid("7701234567", "Alpha LLC", "user@"));
        // все нарушения строки собираются вместе
        check("two problems", VALIDATOR.validate(new ClientDraft("123", "Bad", "bad")).size() == 2);
    }

    private static void checkImporter() throws IOException {
        // открытый файл из задания: 2 принято, строки 3 и 5 отклонены
        ClientRepository repo = new ClientRepository();
        ImportResult r = new ClientImporter(repo).importFile(Paths.get("data/sample.csv"));
        check("sample result", r.accepted() == 2 && r.rejected() == 2 && repo.size() == 2);
        check("sample error lines", r.errors().get(0).lineNumber() == 3 && r.errors().get(1).lineNumber() == 5);
        // ошибка в середине не останавливает импорт
        r = importLines("inn;name;email", "7701234567;Alpha LLC;office@alpha.ru", "123;Bad;bad", "781234567890;Beta Inc;info@beta.com");
        check("continue after error", r.accepted() == 2 && r.rejected() == 1);
        // дубликат ИНН: первая запись остается
        repo = new ClientRepository();
        r = importLines(repo, "inn;name;email", "7701234567;Alpha LLC;office@alpha.ru", "7701234567;Alpha Copy;copy@alpha.ru");
        check("duplicate inn", r.accepted() == 1 && r.rejected() == 1 && r.errors().get(0).lineNumber() == 3);
        check("first record kept", repo.findByInn("7701234567").name().equals("Alpha LLC"));
        // пустой файл и файл только с заголовком
        check("empty file", importLines().accepted() == 0);
        check("only header", importLines("inn;name;email").rejected() == 0);
        check("blank lines skipped", importLines("inn;name;email", "", "7701234567;Alpha LLC;office@alpha.ru", "  ").rejected() == 0);
        // файла нет: IOException, импорт не начинается
        boolean failed = false;
        try {
            new ClientImporter(new ClientRepository()).importFile(Paths.get("data/no_such_file.csv"));
        } catch (IOException e) {
            failed = true;
        }
        check("missing file", failed);
        check("errors copy", !tryChangeErrors(r));
    }

    // ---------- вспомогательные методы ----------

    private static boolean valid(String inn, String name, String email) {
        return VALIDATOR.validate(new ClientDraft(inn, name, email)).isEmpty();
    }

    private static int parseErrorLine(ClientCsvParser parser, String line, int number) {
        try {
            parser.parse(line, number);
            return -1;
        } catch (ImportLineException e) {
            return e.getLineNumber();
        }
    }

    private static ImportResult importLines(String... lines) throws IOException {
        return importLines(new ClientRepository(), lines);
    }

    // строки пишутся во временный файл и импортируются как обычный файл
    private static ImportResult importLines(ClientRepository repo, String... lines) throws IOException {
        Path tmp = Files.createTempFile("clients", ".csv");
        Files.write(tmp, List.of(lines));
        try {
            return new ClientImporter(repo).importFile(tmp);
        } finally {
            Files.delete(tmp);
        }
    }

    private static boolean tryChangeErrors(ImportResult r) {
        try {
            r.errors().clear();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    private static void check(String name, boolean ok) {
        count++;
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            System.out.println("FAIL " + name);
        }
    }

    // ---------- импорт файла inn;name;email ----------

    private static void runFile(String fileName) {
        System.out.println();
        System.out.println("Импорт " + fileName + ":");
        ImportResult r;
        try {
            r = new ClientImporter(new ClientRepository()).importFile(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println("импорт прерван, файл не открыт: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return;
        }
        System.out.println("принято " + r.accepted() + ", отклонено " + r.rejected());
        for (ImportError e : r.errors()) {
            System.out.println("  строка " + e.lineNumber() + ": " + e.message());
        }
    }
}
