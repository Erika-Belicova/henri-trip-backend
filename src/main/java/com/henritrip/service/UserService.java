package com.henritrip.service;

import com.henritrip.dto.user.CreateUserRequestDTO;
import com.henritrip.dto.user.UserResponseDTO;
import com.henritrip.exception.DuplicateEmailException;
import com.henritrip.exception.UserNotFoundException;
import com.henritrip.mapper.UserMapper;
import com.henritrip.model.entity.User;
import com.henritrip.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Service responsible for administrative user management.
 * Handles user retrieval, creation and deletion.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {

        List<User> users = userRepository.findAll();

        return userMapper.toUserResponseDTOList(users);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {

        User user = findUserById(userId);

        return userMapper.toUserResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO requestDTO) {

        // normalize email before checking and persisting
        String normalizedEmail = normalizeEmail(requestDTO.getEmail());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(
                    "A user with this email address already exists"
            );
        }

        // map the request and explicitly set security-sensitive fields
        User user = userMapper.toUserEntity(requestDTO);

        user.setEmail(normalizedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(requestDTO.getPassword())
        );
        user.setRole(requestDTO.getRole());

        // persist the new user
        User savedUser = userRepository.save(user);

        return userMapper.toUserResponseDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {

        User user = findUserById(userId);

        userRepository.delete(user);
    }

    // retrieves a user or throws an exception when the identifier is unknown
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with ID " + userId + " not found"
                        )
                );
    }

    // normalizes email addresses consistently with the database uniqueness index
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}