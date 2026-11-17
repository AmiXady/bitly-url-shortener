package kz.amixady.auth.dto;

import jakarta.validation.constraints.NotBlank;

// UC-03, Б и В — продлить вход, выйти
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {

    @Override
    public String toString() {
        return "RefreshTokenRequest[refreshToken=***]";
    }
}