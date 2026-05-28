package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.response.BaseResponse;
import org.example.service.api.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Login operation
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @return OK for successful authentication
     */
    @PostMapping("/login")
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
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        authService.changePassword(changePasswordRequest);
        return ResponseEntity.ok().build();
    }

}
