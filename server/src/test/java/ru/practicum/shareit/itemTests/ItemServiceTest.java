package ru.practicum.shareit.itemTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemDbService;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserService userService;

    @InjectMocks
    ItemDbService itemService;

    private User itemOwner;
    private Item itemOne;


    @BeforeEach
    public void init() {
        itemOwner = User.builder()
                .id(1L)
                .name("owner")
                .email("owner@email.com")
                .build();
        itemOne = Item.builder()
                .id(1L)
                .name("itemOne")
                .description("itemOne Desc")
                .owner(itemOwner)
                .available(true)
                .bookings(null)
                .comments(null)
                .itemRequest(null)
                .build();
    }

    @Test
    public void createItemSuccessTest() {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("item name")
                .description("item description")
                .available(true)
                .requestId(null)
                .build();

        when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> {
                    Item item = invocationOnMock.getArgument(0);
                    item.setId(2L);
                    return item;
                });
        ItemResponseDto itemResponseDto = itemService.createItem(itemOwner.getId(), itemCreateDto);

        assertEquals(2, itemResponseDto.getId());
        assertEquals("item name", itemResponseDto.getName());
        assertTrue(itemResponseDto.getAvailable());
        verify(itemRepository).save(argThat(item -> item.getOwner().equals(itemOwner)));
    }

    @Test
    public void createItemThrowsNotFoundException() {
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("item name")
                .description("item description")
                .available(true)
                .requestId(null)
                .build();
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, itemCreateDto));
        verify(itemRepository, never()).save(any());
    }

    @Test
    public void updateItemSuccessful() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("itemOne updated")
                .description("itemOne Desc updated")
                .available(false)
                .build();

        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(itemOne));
        when(userService.isUserExist(anyLong()))
                .thenReturn(true);

        ItemResponseDto itemResponseDto = itemService.updateItem(itemOwner.getId(), itemOne.getId(), itemUpdateDto);
        assertEquals("itemOne updated", itemResponseDto.getName());
        assertEquals("itemOne Desc updated", itemResponseDto.getDescription());
        assertFalse(itemResponseDto.getAvailable());
    }

    @Test
    public void getItemByIdSuccessful() {
        when(itemRepository.findById(itemOne.getId()))
                .thenReturn(Optional.of(itemOne));
        when(commentRepository.findAllByItem_Id(itemOne.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.isUserExist(anyLong()))
                .thenReturn(true);


        ItemResponseDto itemResponseDto = itemService.getById(itemOwner.getId(), itemOne.getId());
        assertEquals(itemOne.getId(), itemResponseDto.getId());
        assertEquals(Collections.emptyList(), itemResponseDto.getComments());
    }

    @Test
    public void getAllUserItemsSuccessful() {
        when(itemRepository.findByOwnerId(anyLong()))
                .thenReturn(List.of(itemOne));
        when(commentRepository.findByItemIdIn(List.of(itemOne.getId())))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.getLastApprovedByItemIds(anyList(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.getNextApprovedByItemIds(anyList(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<ItemResponseDto> itemResponseDtos = itemService.getAllUserItems(itemOwner.getId());
        assertEquals(1, itemResponseDtos.size());
        assertEquals(itemOne.getName(), itemResponseDtos.getFirst().getName());

    }

    @Test
    public void searchItemSuccessful() {
        when(userService.isUserExist(anyLong()))
                .thenReturn(true);
        when(itemRepository.searchAvailableItems(itemOwner.getId()))
                .thenReturn(List.of(itemOne));

        List<ItemResponseDto> itemResponseDtos = itemService.searchItem(itemOwner.getId(), "itemOne");
        assertEquals(1, itemResponseDtos.size());
        assertEquals("itemOne", itemResponseDtos.getFirst().getName());
    }

    @Test
    public void createCommentSuccessful() {
        when(itemRepository.findById(itemOne.getId()))
                .thenReturn(Optional.of(itemOne));
        when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
        when(bookingRepository.getCompletedBookings(itemOne.getId(), itemOwner.getId()))
                .thenReturn(List.of(Booking.builder().build()));
        when(commentRepository.save(any(Comment.class)))
                .then(invocationOnMock -> {
                            Comment comment = invocationOnMock.getArgument(0);
                            comment.setId(1L);
                            if (comment.getCreated() == null) comment.setCreated(LocalDateTime.now());
                            return comment;
                        }
                );

        CommentCreateRequestDto commentCreateRequestDto = new CommentCreateRequestDto();
        commentCreateRequestDto.setText("comment");
        CommentResponseDto commentResponseDto = itemService.createComment(
                itemOwner.getId(), itemOne.getId(), commentCreateRequestDto
        );

        assertEquals("comment", commentResponseDto.getText());
        assertEquals(itemOwner.getName(), commentResponseDto.getAuthorName());
    }

    @Test
    public void getCommentsForItemSuccess() {
        Comment comment = new Comment(1L, "comment", itemOne, itemOwner, LocalDateTime.now());
        when(commentRepository.findAllByItem_Id(itemOne.getId()))
                .thenReturn(List.of(comment));

        List<CommentResponseDto> commentResponseDtos = itemService.getCommentsForItem(itemOne.getId());

        assertNotNull(commentResponseDtos);
        assertEquals(comment.getId(), commentResponseDtos.getFirst().getId());
        assertEquals(comment.getText(), commentResponseDtos.getFirst().getText());
    }
}
