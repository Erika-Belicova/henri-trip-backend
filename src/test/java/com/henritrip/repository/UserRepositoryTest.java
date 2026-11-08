package com.henritrip.repository;

import com.henritrip.TestcontainersConfiguration;
import com.henritrip.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailIgnoreCaseShouldReturnUser() {
        // arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("test-password-hash");

        userRepository.save(user);

        // act
        var result = userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM");

        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void existsByEmailIgnoreCaseShouldReturnCorrectResult() {
        // arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("test-password-hash");

        userRepository.save(user);

        // act & assert
        assertThat(userRepository.existsByEmailIgnoreCase("TEST@EXAMPLE.COM"))
                .isTrue();

        assertThat(userRepository.existsByEmailIgnoreCase("unknown@example.com"))
                .isFalse();
    }
}
