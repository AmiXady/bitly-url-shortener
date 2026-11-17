package kz.amixady.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmRequest(

        @NotBlank
        @Size(max = 100)
        String token
) {

    @Override
    public String toString() {
        return "ConfirmRequest[token=***]";
    }
}