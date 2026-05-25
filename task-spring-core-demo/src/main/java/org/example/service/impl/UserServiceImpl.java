package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.aspect.Secured;
import org.example.dao.UserRepository;
import org.example.dto.request.AuthRequest;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.api.PasswordEncoder;
import org.example.service.api.UserService;
import org.example.util.CredentialGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Validated
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get list of UserDTO
     *
     * @param authRequest the instance of AuthRequest containing credentials
     * @return list of users that converted to the list of UserDTO
     */
    @Secured
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAll(AuthRequest authRequest) {
        log.debug("Find All User");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    /**
     * Get UserDTO by user ID
     *
     * @param userId the ID of the user
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if the User not found with given id
     * @return UserDTO corresponding to the given ID
     */
    @Secured
    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long userId, AuthRequest authRequest) {
        log.debug("Find User by ID: {}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));
    }

    /**
     * Get UserDTO by username
     *
     * @param username the username of the user
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if the User not found with given username
     * @return UserDTO corresponding to the given username
     */
    @Secured
    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username, AuthRequest authRequest) {
        log.debug("Find User by username: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("User not found by username: " + username));
    }

    /**
     * Update an existing user profile.
     *
     * @param userId the ID of user
     * @param updateRequest the request object containing updated user details
     * @param authRequest the instance of AuthRequest containing credentials
     * @throws EntityNotFoundException if the User not found with given userId
     * @return updated UserDTO
     */
    @Secured
    @Override
    public UserDTO updateUser(Long userId, UserUpdateRequest updateRequest, AuthRequest authRequest) {
        log.debug("Update Trainee id: {} and request: {}", userId, updateRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));

        // apply changes
        user.setFirstName(updateRequest.firstName());
        user.setLastName(updateRequest.lastName());

        String username = defineNewUsernameForNewUserEntity(user);
        user.setUsername(username);

        userRepository.save(user);

        return userMapper.toDTO(user);
    }

    /**
     * Create a new user
     *
     * @param userCreateRequest the request object containing user details for create
     * @return created UserDTO
     */
    @Override
    public UserDTO createUser(UserCreateRequest userCreateRequest) {
        if(userCreateRequest == null) {
            log.error("UserCreateRequest cannot be null");
            throw new IllegalArgumentException("UserCreateRequest cannot be null");
        }

        log.debug("Create User request: {}", userCreateRequest);

        // User instance without credentials
        User user = userMapper.toEntity(userCreateRequest);

        // set credentials
        String username = defineNewUsernameForNewUserEntity(user);
        user.setUsername(username);
        String rawPassword = CredentialGenerator.generatePassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        userRepository.save(user);

        log.info("User created with username: {}", user.getUsername());
        return userMapper.toDTO(user);
    }

    /**
     * Check if user exists by ID
     *
     * @param userId the id of the user
     * @return true if user found, false is not found
     */
    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Get Reference to User entity
     *
     * @param userId the id of the user
     * @return User reference to User entity
     */
    @Transactional(readOnly = true)
    @Override
    public User getReferenceById(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    /**
     * Change the password of User
     *
     * @param changePasswordRequest the ChangePasswordRequest instance details required to change the password
     * @param authRequest the instance of AuthRequest containing credentials
     */
    @Secured
    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest, AuthRequest authRequest) {
        String rawPassword = changePasswordRequest.password();
        userRepository.changePassword(changePasswordRequest.username(), passwordEncoder.encode(rawPassword));
    }

    /**
     * Define a new username for the new User entity
     *
     * @param user the User entity
     * @return String new username according to user data
     */
    private String defineNewUsernameForNewUserEntity(User user) {
        String username = CredentialGenerator.generateUsername(user.getFirstName(), user.getLastName());

        int serialNumber = 1;
        while(userRepository.existsByUsername(username)) {
            log.debug("User already exists with given username: {}", username);
            username = CredentialGenerator.generateUsernameWithSerial(user.getFirstName(), user.getLastName(), serialNumber);
            serialNumber++;
        }
        return username;
    }
}
