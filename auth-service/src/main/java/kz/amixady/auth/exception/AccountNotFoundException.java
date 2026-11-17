package kz.amixady.auth.exception;

import java.util.UUID;

// аккаунт с таким id не найден — внутренняя ошибка или запрос по id; ответ 404
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID accountId) {
        super("Account " + accountId + " not found");
    }
}