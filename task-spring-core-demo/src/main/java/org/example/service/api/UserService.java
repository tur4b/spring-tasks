package org.example.service.api;

import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
    UserDTO findUserById(Long userId);
    UserDTO findUserByUsername(String username);
    UserDTO createUser(UserCreateRequest userCreateRequest);
    UserDTO updateUser(Long userId, UserUpdateRequest userUpdateRequest);
    boolean deleteUser(Long userId);

    boolean existsById(Long aLong);
}
