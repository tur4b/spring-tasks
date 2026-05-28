package org.example.controller;

import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.response.BaseResponse;
import org.example.service.api.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Api(tags = "Authentication")
public class AuthController {

    private final AuthService authService;

    /**
     * Login operation
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @return OK for successful authentication
     */
    @PostMapping("/login")
    @ApiOperation(value = "Authenticate user", notes = "Validates username and password.")
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
    @ApiOperation(value = "Change user password", notes = "Changes password after old password verification.")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        authService.changePassword(changePasswordRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/base64-token")
    public ResponseEntity<String> getAuthBasicToken(@RequestParam String username, @RequestParam String password) {
        return ResponseEntity.ok(
                "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()))
        );
    }
}
