package kz.amixady.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// UC-03, А — войти
public record LoginRequest(

        @NotBlank
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(max = 128)
        String password
) {

    // пароль не должен попасть в логи
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}