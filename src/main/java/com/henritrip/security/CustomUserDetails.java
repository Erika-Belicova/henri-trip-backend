package com.henritrip.security;

import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Custom implementation of Spring Security's UserDetails,
 * representing authenticated user information based on the application's User entity.
 * Provides the user's ID, email, password hash and assigned role.
 */
public final class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = Objects.requireNonNull(user, "User must not be null");
    }

    public Long getId() {
        return user.getId();
    }

    // returns the original domain enum for application-level operations
    public UserRole getUserRole() {
        return user.getRole();
    }

    public OffsetDateTime getCreatedAt() {
        return user.getCreatedAt();
    }

    public OffsetDateTime getUpdatedAt() {
        return user.getUpdatedAt();
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                // Spring Security authority string for checking role-based permissions
                new SimpleGrantedAuthority("ROLE_" + getUserRole().name())
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    @NonNull
    public String getUsername() {
        return user.getEmail();
    }

}