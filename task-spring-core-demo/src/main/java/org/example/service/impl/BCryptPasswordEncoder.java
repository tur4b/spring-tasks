package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.service.api.PasswordEncoder;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Validated
@Slf4j
public class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int DEFAULT_ROUNDS_COUNT = 10;

    private final int rounds;

    public BCryptPasswordEncoder(int rounds) {
        if(rounds < DEFAULT_ROUNDS_COUNT || rounds > 31) {
            throw new IllegalArgumentException("Invalid rounds: " + rounds);
        }
        this.rounds = rounds;
    }

    public BCryptPasswordEncoder() {
        this(DEFAULT_ROUNDS_COUNT);
    }

    @Override
    public String encode(String rawPassword) {
        String salt = BCrypt.gensalt(rounds);
        return BCrypt.hashpw(rawPassword, salt);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            log.error(">> error for password matching: {}", e.getMessage());
            return false;
        }
    }
}
