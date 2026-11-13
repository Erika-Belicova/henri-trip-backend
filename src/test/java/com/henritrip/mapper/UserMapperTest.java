package com.henritrip.mapper;

import com.henritrip.configuration.ModelMapperConfig;
import com.henritrip.dto.user.CreateUserRequestDTO;
import com.henritrip.dto.user.UserResponseDTO;
import com.henritrip.model.entity.User;
import com.henritrip.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapperConfig().modelMapper();
        userMapper = new UserMapper(modelMapper);
    }

    @Test
    void toUserResponseDTOShouldMapUserFields() {
        // arrange
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(2);
        OffsetDateTime updatedAt = OffsetDateTime.now();

        User user = createUser("user@example.com");
        user.setId(1L);
        user.setRole(UserRole.USER);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        // act
        UserResponseDTO responseDTO = userMapper.toUserResponseDTO(user);

        // assert
        assertThat(responseDTO.getId()).isEqualTo(1L);
        assertThat(responseDTO.getEmail()).isEqualTo("user@example.com");
        assertThat(responseDTO.getRole()).isEqualTo(UserRole.USER);
        assertThat(responseDTO.getCreatedAt()).isEqualTo(createdAt);
        assertThat(responseDTO.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toUserEntityShouldNotMapPasswordOrRole() {
        // arrange
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO();
        requestDTO.setEmail("user@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setRole(UserRole.ADMIN);

        // act
        User user = userMapper.toUserEntity(requestDTO);

        // assert
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void toUserResponseDTOListShouldMapAllUsers() {
        // arrange
        User firstUser = createUser("first@example.com");
        firstUser.setId(1L);

        User secondUser = createUser("second@example.com");
        secondUser.setId(2L);

        List<User> users = List.of(firstUser, secondUser);

        // act
        List<UserResponseDTO> responseDTOs = userMapper.toUserResponseDTOList(users);

        // assert
        assertThat(responseDTOs)
                .hasSize(2)
                .extracting(UserResponseDTO::getEmail)
                .containsExactly("first@example.com", "second@example.com");

        assertThat(responseDTOs)
                .extracting(UserResponseDTO::getId)
                .containsExactly(1L, 2L);
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("test-password-hash");

        return user;
    }
}
