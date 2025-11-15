package ru.practicum.shareit.userTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void save_findAll_findById() {
        User userOne = userRepository.save(User.builder()
                .name("john")
                .email("j@d.com")
                .build()
        );
        User userTwo = userRepository.save(User.builder()
                .name("mary")
                .email("m@d.com")
                .build()
        );

        List<User> all = userRepository.findAll();
        assertEquals(2, all.size());

        Optional<User> byId = userRepository.findById(userOne.getId());
        assertTrue(byId.isPresent());
        assertEquals("john", byId.get().getName());
    }

    @Test
    void deleteUserById_removesUser() {
        User userOne = userRepository.save(User.builder()
                .name("toDel")
                .email("d@d.com")
                .build()
        );
        Long id = userOne.getId();

        userRepository.deleteUserById(id);

        assertTrue(userRepository.findById(id).isEmpty());
        assertEquals(0, userRepository.findAll().size());
    }
}
