package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.exception.handler.GlobalExceptionHandler;
import org.example.exception.model.BadCredentialsException;
import org.example.exception.model.ErrorResponse;
import org.example.service.api.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // ─── POST /auth/login ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login - 200 OK when credentials are valid")
    void login_ValidCredentials_Returns200() throws Exception {
        doNothing().when(authService).authenticate(any(AuthRequest.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("john.doe", "secret"))))
                .andExpect(status().isOk());

        verify(authService).authenticate(any(AuthRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - 401 UNAUTHORIZED when credentials are wrong")
    void login_BadCredentials_Returns401() throws Exception {
        doThrow(new BadCredentialsException("Invalid credentials", ErrorResponse.ErrorPointer.credentials))
                .when(authService).authenticate(any(AuthRequest.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("john.doe", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - 400 BAD REQUEST when username is blank")
    void login_BlankUsername_Returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /auth/login - 400 BAD REQUEST when password is blank")
    void login_BlankPassword_Returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john.doe\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // ─── PUT /auth/change-password ───────────────────────────────────────────

    @Test
    @DisplayName("PUT /auth/change-password - 200 OK when password changed successfully")
    void changePassword_ValidPayload_Returns200() throws Exception {
        doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("john.doe", "oldPass", "newPass"))))
                .andExpect(status().isOk());

        verify(authService).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("PUT /auth/change-password - 401 when old password is wrong")
    void changePassword_WrongOldPassword_Returns401() throws Exception {
        doThrow(new BadCredentialsException("Invalid credentials", ErrorResponse.ErrorPointer.credentials))
                .when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("john.doe", "wrong", "newPass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /auth/change-password - 400 when username is blank")
    void changePassword_BlankUsername_Returns400() throws Exception {
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}

