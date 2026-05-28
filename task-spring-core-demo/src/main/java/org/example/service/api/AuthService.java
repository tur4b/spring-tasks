package org.example.service.api;

import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;

public interface AuthService {
    void authenticate(AuthRequest authRequest);
    void changePassword(ChangePasswordRequest changePasswordRequest);
}
