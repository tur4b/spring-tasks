package org.example.dto.request;

import javax.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Username can't be blank") String username,
        @NotBlank(message = "Old Password can't be blank") String oldPassword,
        @NotBlank(message = "New Password can't be blank") String newPassword
) {
}
