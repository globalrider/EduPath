package com.edupath.qa.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DevPreviewRoleRequest(
        @NotBlank(message = "Choose a portal preview.")
        @Pattern(regexp = "^(JUNIOR|SENIOR)$", message = "Choose Junior or Senior.")
        String role
) {
}
