package ru.practicum.shareit.itemTests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User itemOwner;
    private User requestor;
    private Item itemOne;
    private Item itemTwo;

    @BeforeEach
    public void init() {
        itemOwner = userRepository.save(User.builder()
                .id(null)
                .email("owner@email.com")
                .name("owner")
                .build()
        );
        requestor = userRepository.save(User.builder()
                .id(null)
                .email("requestor@email.com")
                .name("requestor")
                .build()
        );
        itemOne = itemRepository.save(
                Item.builder()
                        .id(null)
                        .name("itemOne")
                        .description("itemOne Desc")
                        .owner(itemOwner)
                        .available(true)
                        .bookings(null)
                        .comments(null)
                        .itemRequest(null)
                        .build()
        );
        itemTwo = itemRepository.save(
                Item.builder()
                        .id(null)
                        .name("itemTwo")
                        .description("itemOne Two")
                        .owner(itemOwner)
                        .available(true)
                        .bookings(null)
                        .comments(null)
                        .itemRequest(null)
                        .build()
        );
    }

    @AfterEach
    public void cleanUp() {
        itemRepository.delete(itemOne);
        itemRepository.delete(itemTwo);
        userRepository.delete(itemOwner);
        userRepository.delete(requestor);
    }

    @Test
    public void findByOwnerIdTest() {
        List<Item> items = itemRepository.findByOwnerId(itemOwner.getId());
        assertEquals(2, items.size());
        assertTrue(items.contains(itemOne));
        assertTrue(items.contains(itemTwo));

    }

    @Test
    public void searchAvailableItemsTest() {
        List<Item> items = itemRepository.searchAvailableItems(itemOwner.getId());
        assertEquals(2, items.size());
    }

    @Test
    public void searchItemsTest() {
        List<Item> items = itemRepository.searchItems("itemTwo");
        assertEquals(1, items.size());
        assertTrue(items.contains(itemTwo));
    }

    @Test
    public void findByItemRequest_IdTest() {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(null)
                .requester(requestor)
                .description("itemRequest Desc")
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(itemRequest);
        itemOne.setItemRequest(itemRequest);
        itemRepository.save(itemOne);

        List<Item> items = itemRepository.findByItemRequest_Id(itemRequest.getId());
        assertEquals(1, items.size());
        assertTrue(items.contains(itemOne));
    }

    @Test
    public void findAllByItemRequest_IdInTest() {
        ItemRequest itemRequestOne = ItemRequest.builder()
                .id(null)
                .requester(requestor)
                .description("itemRequest Desc")
                .created(LocalDateTime.now())
                .build();
        ItemRequest itemRequestTwo = ItemRequest.builder()
                .id(null)
                .requester(requestor)
                .description("itemRequest Desc")
                .created(LocalDateTime.now())
                .build();
        itemRequestRepository.save(itemRequestOne);
        itemRequestRepository.save(itemRequestTwo);
        itemOne.setItemRequest(itemRequestOne);
        itemRepository.save(itemOne);
        itemTwo.setItemRequest(itemRequestTwo);
        itemRepository.save(itemTwo);

        List<Item> items = itemRepository.findAllByItemRequest_IdIn(List.of(
                itemRequestOne.getId(), itemRequestTwo.getId())
        );
        assertEquals(2, items.size());
        assertTrue(items.contains(itemOne));
        assertTrue(items.contains(itemTwo));
    }

}
