package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Base64;

public record AuthRequest(
        @NotBlank(message = "Username can't be blank") String username,
        @NotBlank(message = "Password can't be blank") String password) {

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
