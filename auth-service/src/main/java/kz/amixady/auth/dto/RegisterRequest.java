package kz.amixady.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotNull
        @Size(min = 8, max = 64, message = "пароль — от 8 до 64 символов")   // F-1.3
        String password
) {

    // пароль не должен попасть в логи
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", password=***]";
    }
}