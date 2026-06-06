package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.service.api.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    /**
     * Login operation
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @return OK for successful authentication
     */
    @PostMapping("/login")
    @SecurityRequirements({})
    @Operation(summary = "Authenticate user", description = "Validates username and password.")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        authService.authenticate(authRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * Change an authenticated user's password.
     *
     * @param changePasswordRequest validated old/new password payload
     * @return OK when password is changed successfully
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change user password", description = "Changes password after old password verification.")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        authService.changePassword(changePasswordRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/base64-token")
    @SecurityRequirements({})
    public ResponseEntity<String> getAuthBasicToken(@RequestParam(name = "username") String username,
                                                    @RequestParam(name = "password") String password) {
        return ResponseEntity.ok(
                "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()))
        );
    }
}
