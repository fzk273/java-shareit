package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getUsers();

    UserResponseDto createUser(CreateUserDto createUserDto);

    UserResponseDto updateUser(Long id, UpdateUserDto updateUserDto);

    void deleteUser(Long userId);

    boolean isUserExist(Long id);
}
