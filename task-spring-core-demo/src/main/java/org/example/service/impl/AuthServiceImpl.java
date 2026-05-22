package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.entity.User;
import org.example.service.api.AuthService;
import org.springframework.stereotype.Service;

/**
 * General Authentication operations
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    /**
     * Authenticate according to credentials
     *
     * @param authRequest instance of AuthRequest
     * @throws SecurityException if credentials are invalid
     */
    @Override
    public void authenticate(AuthRequest authRequest) {
        SecurityException securityException = new SecurityException("INvalid credentials");

        User user = userRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> securityException);

        if(!user.getPassword().equals(authRequest.password())) {
            throw securityException;
        }
    }

}
