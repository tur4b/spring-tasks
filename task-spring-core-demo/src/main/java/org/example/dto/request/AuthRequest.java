package org.example.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Base64;

public record AuthRequest(
        @NotBlank(message = "Username can't be blank")
        @Schema(
                description = "Username of the user",
                example = "trainee.admin"
        )
        String username,
        @NotBlank(message = "Password can't be blank")
        @Schema(
                description = "Password of the user",
                example = "admin123"
        )
        String password) {

    /**
     *
     * @param token Base64 format of username and password
     * @return AuthRequest
     */
    public static AuthRequest fromBasicAuth(String token) {
        SecurityException securityException = new SecurityException("Invalid credentials");

        try {
            if (!token.contains("Basic")) {
                throw securityException;
            }
            token = token.replace("Basic ", "");

            byte[] decodedBytes = Base64.getDecoder().decode(token);
            String decodedString = new String(decodedBytes);

            String[] params = decodedString.split(":");
            String username = params[0];
            String password = params[1];

            return new AuthRequest(username, password);
        } catch (Exception e) {
            throw securityException;
        }
    }
}
