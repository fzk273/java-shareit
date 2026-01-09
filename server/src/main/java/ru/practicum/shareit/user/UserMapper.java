package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.model.User;

public class UserMapper {
    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User createUserDtoToEntity(CreateUserDto dto) {
        return User.builder()
                .id(null)
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static User updateUserDtoToEntity(UpdateUserDto dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }
}
