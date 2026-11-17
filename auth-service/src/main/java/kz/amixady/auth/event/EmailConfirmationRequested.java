package kz.amixady.auth.event;

import kz.amixady.auth.entity.EmailAddress;

// UC-01 (шаг 5, 3b) — нужно письмо «Подтвердите адрес»
public record EmailConfirmationRequested(EmailAddress email, String token) {
}