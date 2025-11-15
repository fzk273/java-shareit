package ru.practicum.shareit.requestTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.request.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    ItemRequestService service;

    private User requester;
    private ItemRequest req1;

    @BeforeEach
    void setup() {
        requester = User.builder().id(1L).name("u").email("u@mail.com").build();
        req1 = ItemRequest.builder()
                .id(100L)
                .description("need drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void create_success() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description("need drill").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(inv -> {
            ItemRequest r = inv.getArgument(0);
            return ItemRequest.builder()
                    .id(101L)
                    .description(r.getDescription())
                    .requester(r.getRequester())
                    .created(r.getCreated())
                    .build();
        });

        ItemRequestResponseDto out = service.create(1L, dto);

        assertEquals(101L, out.getId());
        assertEquals("need drill", out.getDescription());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.create(9L, ItemRequestCreateDto.builder().description("x").build()));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getRequestsByUserId_success_enriched() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(req1));

        // items привязанные к запросу
        Item item = Item.builder()
                .id(5L)
                .name("drill")
                .description("desc")
                .available(true)
                .itemRequest(req1)
                .owner(null)
                .comments(null)
                .bookings(null)
                .build();

        when(itemRepository.findAllByItemRequest_IdIn(List.of(100L))).thenReturn(List.of(item));

        List<ItemRequestResponseWithItemsDto> list = service.getRequestsByUserId(1L);

        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).getId());
        assertEquals(1, list.get(0).getItems().size());
        assertEquals("drill", list.get(0).getItems().get(0).getName());
    }

    @Test
    void getRequestById_success() {
        when(itemRequestRepository.findById(100L)).thenReturn(Optional.of(req1));

        Item it = Item.builder()
                .id(7L).name("hammer").description("steel").available(true)
                .itemRequest(req1).owner(null).comments(null).bookings(null).build();

        when(itemRepository.findByItemRequest_Id(100L)).thenReturn(List.of(it));

        ItemRequestResponseWithItemsDto dto = service.getRequestById(100L);

        assertEquals(100L, dto.getId());
        assertEquals(1, dto.getItems().size());
        assertEquals("hammer", dto.getItems().get(0).getName());
    }

    @Test
    void getRequestById_notFound() {
        when(itemRequestRepository.findById(500L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRequestById(500L));
    }

    @Test
    void getAll_success_paged_and_enriched() {
        User other = User.builder().id(2L).name("o").email("o@mail.com").build();
        ItemRequest r2 = ItemRequest.builder()
                .id(200L).description("other need").requester(other).created(LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findByRequesterIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r2)));

        Item it = Item.builder()
                .id(9L).name("saw").description("sharp").available(true)
                .itemRequest(r2).owner(null).comments(null).bookings(null).build();

        when(itemRepository.findAllByItemRequest_IdIn(List.of(200L))).thenReturn(List.of(it));

        List<ItemRequestResponseWithItemsDto> list = service.getAll(1L, 0, 10);

        assertEquals(1, list.size());
        assertEquals(200L, list.get(0).getId());
        assertEquals("saw", list.get(0).getItems().get(0).getName());
    }
}
