package ru.practicum.shareit.userTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.DataConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserDbService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDbService userService;

    private User userOne;

    @BeforeEach
    void setup() {
        userOne = User.builder()
                .id(1L)
                .name("john")
                .email("j@d.com")
                .build();
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));

        UserResponseDto dto = userService.getUserById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("john", dto.getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(100L));
    }

    @Test
    void getUsers_success() {
        User u2 = User.builder().id(2L).name("mary").email("m@d.com").build();
        when(userRepository.findAll()).thenReturn(List.of(userOne, u2));

        var list = userService.getUsers();

        assertEquals(2, list.size());
        assertEquals("mary", list.get(1).getName());
    }

    @Test
    void createUser_success() {
        CreateUserDto req = new CreateUserDto();
        req.setName("alex");
        req.setEmail("a@a.com");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User toSave = inv.getArgument(0);
            return User.builder()
                    .id(10L)
                    .name(toSave.getName())
                    .email(toSave.getEmail())
                    .build();
        });

        UserResponseDto dto = userService.createUser(req);

        assertEquals(10L, dto.getId());
        assertEquals("alex", dto.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_success_changesNameAndEmail() {
        UpdateUserDto req = new UpdateUserDto();
        req.setName("new");
        req.setEmail("new@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
        when(userRepository.getById(1L)).thenReturn(userOne);
        when(userRepository.findAll()).thenReturn(List.of(userOne));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDto dto = userService.updateUser(1L, req);

        assertEquals("new", dto.getName());
        assertEquals("new@mail.com", dto.getEmail());
        verify(userRepository).save(userOne);
    }

    @Test
    void updateUser_notFound() {
        UpdateUserDto req = new UpdateUserDto();
        req.setName("x");

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(2L, req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emailConflict_throwsDataConflict() {
        UpdateUserDto req = new UpdateUserDto();
        req.setEmail("dup@mail.com");

        User other = User.builder().id(2L).name("o").email("dup@mail.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
        when(userRepository.getById(1L)).thenReturn(userOne);
        when(userRepository.findAll()).thenReturn(List.of(userOne, other));

        assertThrows(DataConflictException.class, () -> userService.updateUser(1L, req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_existing_callsRepoDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));

        userService.deleteUser(1L);

        verify(userRepository).deleteUserById(1L);
    }

    @Test
    void deleteUser_absent_noop() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        userService.deleteUser(9L);

        verify(userRepository, never()).deleteUserById(anyLong());
    }

    @Test
    void isUserExist_trueFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userOne));
        when(userRepository.findById(8L)).thenReturn(Optional.empty());

        assertTrue(userService.isUserExist(1L));
        assertFalse(userService.isUserExist(8L));
    }
}
