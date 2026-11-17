# Практическая работа №11. Файлы, исключения и устойчивый пакетный импорт

Климашевский Даниил Александрович, группа ЭФБО-07-25, вариант 4.

Импорт карточек клиентов из файла: чтение через BufferedReader, разбор строки, валидация и сохранение разделены по классам. Ошибка строки записывается с номером и причиной, и импорт продолжается, ошибка открытия файла прерывает импорт целиком. По варианту 4 наименование клиента обязательно и не длиннее 25 символов.

## Сборка и запуск

Нужна Java 21 или новее.

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr08.SelfCheck
```

Или скриптом: `./run_selfcheck.sh` (macOS/Linux), `run_selfcheck.bat` (Windows).

Сначала выполняются проверки, потом обрабатывается `data/sample.csv` (формат `inn;name;email`, первая строка - заголовок).
Свой файл: `java -cp out ru.edu.pr08.SelfCheck data/boundaries.csv`. Можно передать несколько файлов: `java -cp out ru.edu.pr08.SelfCheck data/boundaries.csv data/no_such_file.csv`.

Пакет называется `ru.edu.pr08`, как в исходном проекте.

## Что где лежит

- `src/ru/edu/pr08/ClientImporter.java` - чтение файла и обработка строк
- `src/ru/edu/pr08/ClientCsvParser.java` (и ImportLineException.java) - разбор строки и ошибка строки
- `src/ru/edu/pr08/ClientValidator.java` - правила ИНН, наименования и email
- `src/ru/edu/pr08/ClientRepository.java` - хранение клиентов по ИНН
- `src/ru/edu/pr08/ImportResult.java` (и ImportError.java) - итог импорта
- `src/ru/edu/pr08/Client.java` (и ClientDraft.java) - клиент и разобранная строка
- `src/ru/edu/pr08/SelfCheck.java` - проверки и импорт файлов
- `data/sample.csv` - открытый набор из задания, `data/boundaries.csv` - мои граничные значения
- `diagrams/` - activity-диаграмма и диаграмма классов (dot и png)
- `DECISIONS.md` - вариант, два решения, граничные проверки, ответы на вопросы задания
- `REPORT.docx` - отчет
