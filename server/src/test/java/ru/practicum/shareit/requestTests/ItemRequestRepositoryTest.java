package ru.practicum.shareit.requestTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByRequesterIdOrderByCreatedDesc_ordersProperly() {
        User user = userRepository.save(User.builder().name("u").email("u@mail.com").build());

        ItemRequest itemRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("old")
                .requester(user)
                .created(LocalDateTime.now().minusDays(2))
                .build());
        ItemRequest itemRequest2 = itemRequestRepository.save(ItemRequest.builder()
                .description("new")
                .requester(user)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequest> list = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(user.getId());

        assertEquals(2, list.size());
        assertEquals(itemRequest2.getId(), list.get(0).getId());
        assertEquals(itemRequest.getId(), list.get(1).getId());
    }

    @Test
    void findByRequesterIdNot_withPaging_returnsOtherUsersRequests() {
        User u1 = userRepository.save(
                User.builder().name("u1").email("u1@mail.com")
                        .build()
        );
        User u2 = userRepository.save(
                User.builder()
                        .name("u2")
                        .email("u2@mail.com")
                        .build()
        );

        itemRequestRepository.save(ItemRequest.builder()
                .description("req u2 a")
                .requester(u2)
                .created(LocalDateTime.now())
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .description("req u2 b")
                .requester(u2)
                .created(LocalDateTime.now().minusHours(1))
                .build());

        Page<ItemRequest> page = itemRequestRepository.findByRequesterIdNot(
                u1.getId(), PageRequest.of(0, 10));

        assertEquals(2, page.getContent().size());
        assertTrue(page.getContent().stream().allMatch(r -> r.getRequester().getId().equals(u2.getId())));
    }
}
