package ru.practicum.shareit.user.dto.response;

import ru.practicum.shareit.user.User;

public class UserResponseMapper {
    public static UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
