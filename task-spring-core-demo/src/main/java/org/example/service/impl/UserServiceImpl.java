package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserDAO;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.util.IdGenerator;
import org.example.service.api.UserService;
import org.example.util.CredentialGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final UserMapper userMapper;
    private final IdGenerator idGenerator;

    /**
     * Constructor for <code>UserServiceImpl</code>
     * constructor injection applied for required dependencies
     *
     * @param userDAO UserDAO instance
     * @param userMapper UserMapper instance
     */
    public UserServiceImpl(UserDAO userDAO,
                           UserMapper userMapper, IdGenerator idGenerator) {
        this.userDAO = userDAO;
        this.userMapper = userMapper;
        this.idGenerator = idGenerator;
    }


    /**
     * Get list of UserDTO
     *
     * @return list of users that converted to dtos
     */
    @Override
    public List<UserDTO> findAll() {
        log.debug("Find All User");
        return userDAO.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * Get UserDTO by user ID
     *
     * @param userId the ID of the user
     * @return UserDTO corresponding to the given ID
     */
    @Override
    public UserDTO findUserById(Long userId) {
        log.debug("Find User by ID: {}", userId);
        return userDAO.findById(userId)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found by id: " + userId));
    }

    /**
     * Get UserDTO by username
     *
     * @param username the username of the user
     * @return UserDTO corresponding to the given username
     */
    @Override
    public UserDTO findUserByUsername(String username) {
        log.debug("Find User by username: {}", username);
        return userDAO.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found by username: " + username));
    }

    @Override
    public UserDTO createUser(UserCreateRequest userCreateRequest) {
        if(userCreateRequest == null) {
            log.error("UserCreateRequest cannot be null");
            throw new IllegalArgumentException("UserCreateRequest cannot be null");
        }

        log.debug("Create User reuqest: {}", userCreateRequest);

        // User instance without credentials
        User user = userMapper.toEntity(userCreateRequest);

        String username = CredentialGenerator.generateUsername(user.getFirstName(), user.getLastName());

        int serialNumber = 1;
        while(userDAO.existsByUsername(username)) {
            log.debug("User already exists with given username: {}", username);
            username = CredentialGenerator.generateUsernameWithSerial(user.getFirstName(), user.getLastName(), serialNumber);
            serialNumber++;
        }

        // set credentials
        user.setId(idGenerator.getNextId(User.class.getSimpleName()));
        user.setUsername(username);
        user.setPassword(CredentialGenerator.generatePassword());

        userDAO.create(user);

        log.debug("User created: {}", user);
        return userMapper.toDTO(user);
    }

    /**
     * Update an existing user profile.
     *
     * @param userId the ID of user
     * @param updateRequest the request object containing updated user details
     * @return updated UserDTO
     */
    @Override
    public UserDTO updateUser(Long userId, UserUpdateRequest updateRequest) {
        if(userId == null || updateRequest == null) {
            log.error("User ID and UserUpdateRequest cannot be null");
            throw new IllegalArgumentException("User id and UserUpdateRequest cannot be null");
        }

        log.debug("Update Trainee id: {} and request: {}", userId, updateRequest);

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found by id: " + userId));

        // apply changes
        user.setFirstName(updateRequest.firstName());
        user.setLastName(updateRequest.lastName());
        user.setUpdatedAt(LocalDateTime.now());

        userDAO.update(user);

        return userMapper.toDTO(user);
    }

    /**
     * Soft Delete user by ID
     *
     * @param userId the ID of user
     * @return true if deletion was successful, false is not
     */
    @Override
    public boolean deleteUser(Long userId) {
        return userDAO.deleteById(userId);
    }


    /**
     * Check if user exists by ID
     * @param userId the id of the user
     * @return true if user found, false is not found
     */
    @Override
    public boolean existsById(Long userId) {
        return userDAO.existsById(userId);
    }
}
