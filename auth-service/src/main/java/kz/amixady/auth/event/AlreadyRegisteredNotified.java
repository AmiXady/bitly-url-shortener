package kz.amixady.auth.event;

import kz.amixady.auth.entity.EmailAddress;

// UC-01 (3a) — нужно письмо «У вас уже есть аккаунт»
public record AlreadyRegisteredNotified(EmailAddress email) {
}