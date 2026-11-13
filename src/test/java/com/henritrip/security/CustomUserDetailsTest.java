package com.henritrip.security;

import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void shouldReturnUserInformation() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole(UserRole.ADMIN);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertEquals(1L, userDetails.getId());
        assertEquals("admin@example.com", userDetails.getUsername());
        assertEquals("encoded-password", userDetails.getPassword());
        assertEquals(UserRole.ADMIN, userDetails.getUserRole());
    }

    @Test
    void shouldReturnAdminAuthority() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setRole(UserRole.ADMIN);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertEquals(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                userDetails.getAuthorities()
        );
    }

    @Test
    void shouldReturnUserAuthority() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertEquals(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")),
                userDetails.getAuthorities()
        );
    }

    @Test
    void shouldReturnUserTimestamps() {
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(2);
        OffsetDateTime updatedAt = OffsetDateTime.now();

        User user = new User();
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        assertEquals(createdAt, userDetails.getCreatedAt());
        assertEquals(updatedAt, userDetails.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new CustomUserDetails(null)
        );

        assertEquals("User must not be null", exception.getMessage());
    }

}
