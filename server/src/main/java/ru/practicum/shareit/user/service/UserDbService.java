package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.DataConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier("UserDbService")
public class UserDbService implements UserService {
    private final UserRepository userRepository;

    public UserDbService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("There is no such user with id: " + id)
        );
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toDto).toList();
    }

    @Override
    public UserResponseDto createUser(CreateUserDto createUserDto) {
        User user = userRepository.save(UserMapper.createUserDtoToEntity(createUserDto));
        return UserMapper.toDto(user);
    }

    @Transactional
    @Override
    public UserResponseDto updateUser(Long id, UpdateUserDto updateUserDto) {

        if (!isUserExist(id)) {
            throw new NotFoundException("There is no such user with id: " + id);
        }
        User currentUser = userRepository.getById(id);
        User fromDtoUser = UserMapper.updateUserDtoToEntity(updateUserDto);
        if (fromDtoUser.getEmail() != null && !fromDtoUser.getEmail().isEmpty()) {
            currentUser.setEmail(fromDtoUser.getEmail());
        }
        if (fromDtoUser.getName() != null && !fromDtoUser.getName().isEmpty()) {
            currentUser.setName(fromDtoUser.getName());
        }

        List<String> allEmails = userRepository.findAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
        allEmails.remove(currentUser.getEmail());
        if (allEmails.contains(fromDtoUser.getEmail())) {
            throw new DataConflictException("User with email: " + fromDtoUser.getEmail() + " already exists in the database");
        }
        userRepository.save(currentUser);
        return UserMapper.toDto(currentUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (isUserExist(userId)) {

            userRepository.deleteUserById(userId);
        }
    }

    @Override
    public boolean isUserExist(Long id) {
        return userRepository.findById(id).isPresent();
    }
}
