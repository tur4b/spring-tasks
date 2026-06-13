package org.example.service.api;

import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;

public interface AuthService {
    void authenticate(AuthRequest authRequest);
    String login(AuthRequest authRequest);
    void logout(String bearerToken);
    void changePassword(ChangePasswordRequest changePasswordRequest);
}
