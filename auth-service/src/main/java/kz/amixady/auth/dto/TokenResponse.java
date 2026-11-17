package kz.amixady.auth.dto;

import kz.amixady.auth.token.AccessToken;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {

    public static TokenResponse bearer(AccessToken accessToken, String refreshToken) {
        return new TokenResponse(
                accessToken.value(),
                refreshToken,
                "Bearer",
                accessToken.expiresIn().toSeconds());
    }

    // ключи — секреты, в логи не должны попадать
    @Override
    public String toString() {
        return "TokenResponse[accessToken=***, refreshToken=***, tokenType=" + tokenType
                + ", expiresIn=" + expiresIn + "]";
    }
}