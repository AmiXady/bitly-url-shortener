package kz.amixady.auth.controller;

import jakarta.validation.Valid;
import kz.amixady.auth.dto.*;
import kz.amixady.auth.service.token.JwtClaims;
import kz.amixady.auth.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    // UC-01 — всегда 202, даже если email занят (BR-3)
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        try {
            registerUseCase.execute(request);
        } catch (DataIntegrityViolationException e) {
            // гонка двух регистраций с одним email — отвечаем как обычно, чтобы не выдать, что email занят
        }
    }

    // UC-02, А — подтвердить email по токену из письма
    @PostMapping("/confirm")
    public void confirm(@Valid @RequestBody ConfirmRequest request) {
        confirmEmailUseCase.confirm(request.token());
    }

    // UC-02, Б — отправить письмо ещё раз; всегда 202 (BR-5)
    @PostMapping("/confirm/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendConfirmation(@Valid @RequestBody ResendRequest request) {
        confirmEmailUseCase.resend(request.email());
    }

    // UC-03, А — войти
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return loginUseCase.login(request);
    }

    // UC-03, Б — продлить вход
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return loginUseCase.refresh(request.refreshToken());
    }

    // UC-20 — выйти; повторный выход — тоже 204
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.execute(request.refreshToken());
    }

    // UC-04, А — запросить ссылку сброса; всегда 202 (BR-2)
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        resetPasswordUseCase.requestLink(request);
    }

    // UC-04, Б — задать новый пароль по ссылке из письма
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.reset(request);
    }

    // UC-05 — сменить пароль; кто и какая сессия — только из JWT
    @PostMapping("/password/change")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        UUID sessionId = UUID.fromString(jwt.getClaimAsString(JwtClaims.SESSION_ID));
        changePasswordUseCase.execute(request, accountId, sessionId);
    }
}