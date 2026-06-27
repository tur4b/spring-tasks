package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.security.JwtService;
import org.example.config.security.LogoutTokenStore;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.entity.User;
import org.example.exception.model.BadCredentialsException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * General Authentication operations
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final LogoutTokenStore logoutTokenStore;

    /**
     * Authenticate according to credentials
     *
     * @param authRequest instance of AuthRequest
     * @param ipAddress   originating client IP address used for rate-limiting
     * @throws SecurityException if credentials are invalid
     */
    @Override
    @Transactional(readOnly = true)
    public void authenticate(AuthRequest authRequest, String ipAddress) {
        BadCredentialsException badCredentialsException = new BadCredentialsException("Invalid credentials",
                ErrorResponse.ErrorPointer.credentials);

        loginAttemptService.validateNotBlocked(ipAddress);

        User user = userRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> {
                    loginAttemptService.onFailedLogin(ipAddress);
                    return badCredentialsException;
                });

        if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            loginAttemptService.onFailedLogin(ipAddress);
            throw badCredentialsException;
        }

        loginAttemptService.onSuccessfulLogin(ipAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public String login(AuthRequest authRequest, String ipAddress) {
        authenticate(authRequest, ipAddress);
        return jwtService.generateToken(authRequest.username());
    }
    
    @Override
    public void logout(String bearerToken) {
        BadCredentialsException badCredentialsException = new BadCredentialsException("Invalid token",
                ErrorResponse.ErrorPointer.credentials);

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw badCredentialsException;
        }

        String token = bearerToken.substring(7);
        if (!jwtService.isTokenValid(token)) {
            throw badCredentialsException;
        }

        logoutTokenStore.invalidateToken(token, jwtService.extractExpiration(token).toInstant());
    }

    /**
     * Change the password for the user identified in the request.
     * Verifies the old password before applying the new hashed password.
     *
     * @param changePasswordRequest payload containing username, old password, and new password
     * @throws org.example.exception.model.BadCredentialsException if the old password does not match
     */
    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        BadCredentialsException badCredentialsException = new BadCredentialsException("Invalid credentials",
                ErrorResponse.ErrorPointer.credentials);

        User user = userRepository.findByUsername(changePasswordRequest.username())
                        .orElseThrow(() -> badCredentialsException);

        if(!passwordEncoder.matches(changePasswordRequest.oldPassword(),  user.getPassword())) {
            throw badCredentialsException;
        }
        userRepository.changePassword(changePasswordRequest.username(), passwordEncoder.encode(changePasswordRequest.newPassword()));
    }

}
