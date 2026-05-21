package org.example.service.api;

import org.example.dto.request.AuthRequest;

public interface AuthService {
    void authenticate(AuthRequest authRequest);
}
