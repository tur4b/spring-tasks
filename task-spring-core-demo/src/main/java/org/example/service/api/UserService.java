package org.example.service.api;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserCredentialsDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.User;

import java.util.List;

public interface UserService {

    List<UserDTO> findAll();

    UserDTO findById(Long userId);

    UserDTO findByUsername(String username);

    UserCredentialsDTO createUser(UserCreateRequest userCreateRequest);

    UserDTO updateUser(Long userId, UserUpdateRequest userUpdateRequest);

    boolean existsById(Long userId);

    User getReferenceById(Long userId);

}
