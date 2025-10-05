package ru.practicum.shareit.user.dto.request;

import ru.practicum.shareit.user.User;

public class UserRequestMapper {
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
