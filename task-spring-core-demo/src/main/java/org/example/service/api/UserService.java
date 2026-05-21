package org.example.service.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;

import java.util.List;

public interface UserService {

    List<UserDTO> findAll(@Valid AuthRequest authRequest);

    UserDTO findById(@NotNull(message = "User id can't be null") Long userId,
                     @Valid AuthRequest authRequest);

    UserDTO findByUsername(@NotBlank(message = "Username can't be blank") String username,
                           @Valid AuthRequest authRequest);

    UserDTO createUser(@Valid UserCreateRequest userCreateRequest);

    UserDTO updateUser(@NotNull(message = "User id can't be null")  Long userId,
                       @Valid UserUpdateRequest userUpdateRequest,
                       @Valid AuthRequest authRequest);

    boolean existsById(@NotNull(message = "User id can't be null") Long userId);

    User getReferenceById(@NotNull(message = "User id can't be null") Long userId);

    void changePassword(@Valid ChangePasswordRequest changePasswordRequest,
                        @Valid AuthRequest authRequest);

}
