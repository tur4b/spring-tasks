package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserRepository;
import org.example.dto.request.UserCreateRequest;
import org.example.dto.request.UserUpdateRequest;
import org.example.dto.response.UserCredentialsDTO;
import org.example.dto.response.UserDTO;
import org.example.entity.User;
import org.example.exception.model.NotFoundException;
import org.example.exception.model.ErrorResponse;
import org.example.mapper.UserMapper;
import org.example.service.api.UserService;
import org.example.util.CredentialGenerator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Validated
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get list of UserDTO
     *
     * @return list of users that converted to the list of UserDTO
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
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
     * @throws NotFoundException if the User not found with given id
     * @return UserDTO corresponding to the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long userId) {
        log.debug("Find User by ID: {}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new NotFoundException(
                        "User not found by id: " + userId,
                        ErrorResponse.ErrorPointer.id)
                );
    }

    /**
     * Get UserDTO by username
     *
     * @param username the username of the user
     * @throws NotFoundException if the User not found with given username
     * @return UserDTO corresponding to the given username
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO findByUsername(String username) {
        log.debug("Find User by username: {}", username);
        return userRepository.findByUsername(username)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new NotFoundException(
                        "User not found by username: " + username,
                        ErrorResponse.ErrorPointer.username)
                );
    }

    /**
     * Update an existing user profile.
     *
     * @param userId the ID of user
     * @param updateRequest the request object containing updated user details
     * @throws NotFoundException if the User not found with given userId
     * @return updated UserDTO
     */
    @Override
    public UserDTO updateUser(Long userId, UserUpdateRequest updateRequest) {
        log.debug("Update Trainee id: {} and request: {}", userId, updateRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found by id: " + userId,
                        ErrorResponse.ErrorPointer.id)
                );

        // apply changes
        user.setFirstName(updateRequest.firstName());
        user.setLastName(updateRequest.lastName());

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
    public UserCredentialsDTO createUser(UserCreateRequest userCreateRequest) {
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
        return new UserCredentialsDTO(user.getId(), username, rawPassword);
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isActive())
                .authorities("ROLE_USER")
                .build();
    }
}
