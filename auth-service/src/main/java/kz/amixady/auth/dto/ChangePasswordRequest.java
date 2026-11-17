package kz.amixady.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        @Size(max = 128)
        String currentPassword,
        @NotNull
        @Size(min = 8, max = 64, message = "пароль — от 8 до 64 символов")
        String newPassword
) {

    @AssertTrue(message = "Новый пароль совпадает с текущим")
    public boolean isNewPasswordDifferent() {
        return currentPassword == null || newPassword == null || !newPassword.equals(currentPassword);
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest[currentPassword=***, newPassword=***]";
    }
}