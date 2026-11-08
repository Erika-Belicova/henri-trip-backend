package com.henritrip.mapper;

import com.henritrip.dto.user.CreateUserRequestDTO;
import com.henritrip.dto.user.UserResponseDTO;
import com.henritrip.model.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User entity and its DTOs.
 */
@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        // configure password hash and role to be skipped during automatic mapping
        modelMapper.typeMap(CreateUserRequestDTO.class, User.class)
                .addMappings(mapper -> {
                    mapper.skip(User::setPasswordHash);
                    mapper.skip(User::setRole);
                });
    }

    // convert a User entity to a UserResponseDTO
    public UserResponseDTO toUserResponseDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    // convert a CreateUserRequestDTO to a User entity
    public User toUserEntity(CreateUserRequestDTO requestDTO) {
        return modelMapper.map(requestDTO, User.class);
    }

    // convert a list of User entities to a list of UserResponseDTOs
    public List<UserResponseDTO> toUserResponseDTOList(List<User> users) {
        return users.stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

}
