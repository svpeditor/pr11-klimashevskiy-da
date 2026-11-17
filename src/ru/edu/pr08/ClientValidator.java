package ru.edu.pr08;

import java.util.ArrayList;
import java.util.List;

public class ClientValidator {
    // вариант 4: наименование - обязательное поле, не длиннее 25 символов
    public static final int MAX_NAME_LENGTH = 25;

    // возвращает список нарушений, пустой список - запись корректна
    public List<String> validate(ClientDraft d) {
        List<String> problems = new ArrayList<>();
        if (!d.inn().matches("\\d{10}|\\d{12}")) {
            problems.add("ИНН должен содержать 10 или 12 цифр: " + d.inn());
        }
        if (d.name().isEmpty()) {
            problems.add("не заполнено наименование");
        } else if (d.name().length() > MAX_NAME_LENGTH) {
            problems.add("наименование длиннее " + MAX_NAME_LENGTH + " символов: " + d.name().length());
        }
        if (!isEmail(d.email())) {
            problems.add("неверный email: " + d.email());
        }
        return problems;
    }

    // ровно один @ и непустые части слева и справа
    private boolean isEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@')) {
            return false;
        }
        return at < email.length() - 1;
    }
}
