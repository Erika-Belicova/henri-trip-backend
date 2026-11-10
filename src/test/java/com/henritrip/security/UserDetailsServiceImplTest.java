package com.henritrip.security;

import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import com.henritrip.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);

        user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("encodedPassword");
        user.setRole(UserRole.USER);
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        // arrange
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));

        // act
        UserDetails result =
                userDetailsService.loadUserByUsername("test@example.com");

        // assert
        assertNotNull(result);
        assertInstanceOf(CustomUserDetails.class, result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    void loadUserByUsernameShouldTrimEmailBeforeSearching() {
        // arrange
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(user));

        // act
        UserDetails result =
                userDetailsService.loadUserByUsername("  test@example.com  ");

        // assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());

        verify(userRepository).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenUserDoesNotExist() {
        // arrange
        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
                .thenReturn(Optional.empty());

        // act & assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(
                        "unknown@example.com"
                )
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmailIgnoreCase("unknown@example.com");
    }

}
