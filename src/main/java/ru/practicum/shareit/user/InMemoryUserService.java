package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareit.user.dto.request.UserRequestMapper;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.dto.response.UserResponseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InMemoryUserService implements UserService {
    public final HashMap<Long, User> userList;
    public Long userCounter = 1L;

    public InMemoryUserService(HashMap<Long, User> userList) {
        this.userList = new HashMap<>();
    }


    @Override
    public UserResponseDto getUserById(Long id) {
        if (!isUserExist(id)) {
            throw new NotFoundException("There is no such user with id: " + id);
        }
        return UserResponseMapper.toDto(userList.get(id));
    }

    @Override
    public List<UserResponseDto> getUsers() {
        return userList.values().stream()
                .map(UserResponseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!isUserExist(id)) {
            throw new NotFoundException("There is no such user with id: " + id);
        }
        userList.remove(id);
    }

    @Override
    public UserResponseDto updateUser(Long id, UpdateUserDto updateUserDto) {
        if (!isUserExist(id)) {
            throw new NotFoundException("There is no such user with id: " + id);
        }
        User currentUser = userList.get(id);
        User fromDtoUser = UserRequestMapper.updateUserDtoToEntity(updateUserDto);
        if (fromDtoUser.getEmail() != null && !fromDtoUser.getEmail().isEmpty()) {
            currentUser.setEmail(fromDtoUser.getEmail());
        }
        if (fromDtoUser.getName() != null && !fromDtoUser.getName().isEmpty()) {
            currentUser.setName(fromDtoUser.getName());
        }

        List<String> allEmails = userList.values().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
        allEmails.remove(currentUser.getEmail());
        if (allEmails.contains(fromDtoUser.getEmail())) {
            throw new DataConflictException("User with email: " + fromDtoUser.getEmail() + " already exists in the database");
        }
        userList.replace(id, currentUser);
        return UserResponseMapper.toDto(currentUser);
    }

    @Override
    public UserResponseDto createUser(CreateUserDto createUserDto) {
        User newUser = UserRequestMapper.createUserDtoToEntity(createUserDto);
        if (isMailExist(newUser.getEmail())) {
            throw new DataConflictException("Email: " + newUser.getEmail() + " already exists in the database");
        }
        newUser.setId(userCounter);
        userList.put(userCounter, newUser);
        userCounter++;
        return UserResponseMapper.toDto(newUser);
    }

    private boolean isUserExist(Long id) {
        return userList.get(id) != null;
    }

    private boolean isMailExist(String email) {
        List<String> allEmails = userList.values().stream()
                .map(User::getEmail)
                .toList();
        return allEmails.contains(email);
    }
}
