package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.entity.User;
import org.example.exception.model.BadCredentialsException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.AuthService;
import org.example.service.api.PasswordEncoder;
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

    /**
     * Authenticate according to credentials
     *
     * @param authRequest instance of AuthRequest
     * @throws SecurityException if credentials are invalid
     */
    @Override
    @Transactional(readOnly = true)
    public void authenticate(AuthRequest authRequest) {
        BadCredentialsException badCredentialsException = new BadCredentialsException("Invalid credentials",
                ErrorResponse.ErrorPointer.credentials);

        User user = userRepository.findByUsername(authRequest.username())
                .orElseThrow(() -> badCredentialsException);

        if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
            throw badCredentialsException;
        }
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
