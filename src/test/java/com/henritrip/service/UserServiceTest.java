package com.henritrip.service;

import com.henritrip.dto.user.CreateUserRequestDTO;
import com.henritrip.dto.user.UserResponseDTO;
import com.henritrip.exception.DuplicateEmailException;
import com.henritrip.exception.UserNotFoundException;
import com.henritrip.mapper.UserMapper;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import com.henritrip.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    private User user;
    private UserResponseDTO userResponse;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);

        // configure a predictable user and response
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole(UserRole.USER);

        OffsetDateTime now = OffsetDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        userResponse = new UserResponseDTO();
        userResponse.setId(1L);
        userResponse.setEmail("user@example.com");
        userResponse.setRole(UserRole.USER);
        userResponse.setCreatedAt(now);
        userResponse.setUpdatedAt(now);

        userService = new UserService(
                userRepository,
                userMapper,
                passwordEncoder
        );
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        // arrange: configure existing users and their mapped responses
        List<User> users = List.of(user);
        List<UserResponseDTO> responses = List.of(userResponse);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserResponseDTOList(users)).thenReturn(responses);

        // act: retrieve all users
        List<UserResponseDTO> result = userService.getAllUsers();

        // assert: verify that all users are returned
        assertEquals(1, result.size());
        assertEquals(userResponse.getEmail(), result.getFirst().getEmail());

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseDTOList(users);
    }

    @Test
    void getUserByIdShouldReturnUserWhenUserExists() {
        // arrange: configure an existing user
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponse);

        // act: retrieve the user
        UserResponseDTO result = userService.getUserById(1L);

        // assert: verify the returned user information
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user@example.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByIdShouldThrowExceptionWhenUserDoesNotExist() {
        // arrange: configure an unknown user identifier
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // act & assert: verify that the appropriate exception is thrown
        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(99L)
        );

        verify(userRepository).findById(99L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void createUserShouldPersistUserWithNormalizedEmailAndEncodedPassword() {
        // arrange: configure a valid creation request
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail(" User@Example.com ");
        request.setPassword("Password1234+");
        request.setRole(UserRole.ADMIN);

        User mappedUser = new User();
        when(userMapper.toUserEntity(request)).thenReturn(mappedUser);
        when(userRepository.existsByEmailIgnoreCase("user@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Password1234+"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponse);

        // act: create the user
        UserResponseDTO result = userService.createUser(request);

        // assert: verify the response and persisted user configuration
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());

        assertEquals("user@example.com", mappedUser.getEmail());
        assertEquals("encoded-password", mappedUser.getPasswordHash());
        assertEquals(UserRole.ADMIN, mappedUser.getRole());

        verify(passwordEncoder).encode("Password1234+");
        verify(userRepository).save(mappedUser);
    }

    @Test
    void createUserShouldThrowExceptionWhenEmailAlreadyExists() {
        // arrange: configure a creation request with an existing email
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setEmail("user@example.com");
        request.setPassword("Password1234+");
        request.setRole(UserRole.USER);

        when(userRepository.existsByEmailIgnoreCase("user@example.com"))
                .thenReturn(true);

        // act & assert: verify that duplicate creation is rejected
        assertThrows(
                DuplicateEmailException.class,
                () -> userService.createUser(request)
        );

        // verify that no user is mapped, encoded or persisted
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(userMapper, passwordEncoder);
    }

    @Test
    void deleteUserShouldDeleteUserWhenUserExists() {
        // arrange: configure an existing user
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // act: delete the user
        userService.deleteUser(1L);

        // assert: verify that the user is deleted
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldThrowExceptionWhenUserDoesNotExist() {
        // arrange: configure an unknown user identifier
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // act & assert: verify that deletion is rejected
        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(99L)
        );

        verify(userRepository, never()).delete(any(User.class));
    }

}
