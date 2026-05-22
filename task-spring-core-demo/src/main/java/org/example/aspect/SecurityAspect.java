package org.example.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.example.dto.request.AuthRequest;
import org.example.service.api.AuthService;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
@Aspect
public class SecurityAspect {

    private final AuthService authService;

    @Before("@annotation(org.example.aspect.Secured)")
    public void applySecurityChecks(JoinPoint jp) {
        log.debug(">> applying security checks...");

        Object[] args = jp.getArgs();

        AuthRequest authRequest = Arrays.stream(args)
                .filter(arg -> arg instanceof AuthRequest)
                .map(arg -> (AuthRequest) arg)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid method structure. @Secured requires AuthRequest param for the method"));

        // check credentials
        authService.authenticate(authRequest);

        log.debug(">> applying security checks... done");
    }
}
