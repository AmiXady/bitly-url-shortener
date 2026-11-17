package kz.amixady.auth.event;

import kz.amixady.auth.entity.EmailAddress;

public record PasswordChanged(EmailAddress email) {
}