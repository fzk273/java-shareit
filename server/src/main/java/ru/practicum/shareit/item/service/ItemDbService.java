package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comments.dto.CommentMapper;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.comments.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Qualifier("ItemDbService")
@Service
@Slf4j
public class ItemDbService implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;


    public ItemDbService(ItemRepository itemRepository,
                         @Qualifier("UserDbService") UserService userService,
                         UserRepository userRepository, CommentRepository commentRepository, BookingRepository bookingRepository, ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Transactional
    @Override
    public ItemResponseDto createItem(Long userId, ItemCreateDto dto) {
        User itemOwner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no such user with id: " + userId));

        Item item;

        if (dto.getRequestId() != null) {
            ItemRequest req = itemRequestRepository.findById(dto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("There is no request with id: " + dto.getRequestId()));
            item = ItemMapper.itemCreateRequestToEntity(dto, req);
        } else {
            item = ItemMapper.itemCreateRequestToEntity(dto);
        }

        item.setOwner(itemOwner);

        Item saved = itemRepository.save(item);

        return ItemMapper.itemToResponseDto(saved);
    }

    @Transactional
    @Override
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto dto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }

        Item currentItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("There is no such item with id: " + itemId));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            currentItem.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            currentItem.setDescription(dto.getDescription().trim());
        }
        if (dto.getAvailable() != null) {
            currentItem.setAvailable(dto.getAvailable());
        }

        Item saved = itemRepository.save(currentItem);
        return ItemMapper.itemToResponseDto(saved);
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("There is no such item with id: " + itemId));

        List<Comment> comments = commentRepository.findAllByItem_Id(itemId);

        Long lastId = null;
        Long nextId = null;

        if (Objects.equals(item.getOwner().getId(), userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastList = bookingRepository.getLastApprovedByItemIds(List.of(itemId), now);
            if (!lastList.isEmpty()) lastId = lastList.getFirst().getId();

            List<Booking> nextList = bookingRepository.getNextApprovedByItemIds(List.of(itemId), now);
            if (!nextList.isEmpty()) nextId = nextList.getFirst().getId();
        }

        return ItemMapper.itemToResponseDtoWithAllFields(item, lastId, nextId, comments);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();

        Map<Long, List<Comment>> commentsByItemId = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        Map<Long, Long> lastBookingIdByItemId = new HashMap<>();
        for (Booking b : bookingRepository.getLastApprovedByItemIds(itemIds, now)) {
            Long iid = b.getItem().getId();
            lastBookingIdByItemId.putIfAbsent(iid, b.getId());
        }

        Map<Long, Long> nextBookingIdByItemId = new HashMap<>();
        for (Booking b : bookingRepository.getNextApprovedByItemIds(itemIds, now)) {
            Long iid = b.getItem().getId();
            nextBookingIdByItemId.putIfAbsent(iid, b.getId());
        }

        return items.stream()
                .map(it -> ItemMapper.itemToResponseDtoWithAllFields(
                        it,
                        lastBookingIdByItemId.get(it.getId()),
                        nextBookingIdByItemId.get(it.getId()),
                        commentsByItemId.getOrDefault(it.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public List<ItemResponseDto> searchItem(Long userId, String text) {
        String safeString = text == null ? "" : text.trim();
        if (safeString.isEmpty()) return Collections.emptyList();

        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }
        List<Item> userItems = itemRepository.searchAvailableItems(userId);
        if (userItems.isEmpty()) return Collections.emptyList();

        String safeStringLowerCase = safeString.toLowerCase(Locale.ROOT);

        return userItems.stream()
                .filter(item -> containsIgnoreCase(item.getName(), safeStringLowerCase)
                        || containsIgnoreCase(item.getDescription(), safeStringLowerCase))
                .map(ItemMapper::itemToResponseDto)
                .toList();
    }

    private static boolean containsIgnoreCase(String field, String strLower) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(strLower);
    }

    @Transactional
    @Override
    public CommentResponseDto createComment(Long userId, Long itemId, CommentCreateRequestDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("There is no item with id: " + itemId));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("There is no user with id: " + userId));

        List<Booking> bookings = bookingRepository.getCompletedBookings(itemId, userId);
        if (bookings.isEmpty()) {
            throw new BadRequestException("User is not renting this item");
        }
        Comment comment = CommentMapper.toEntity(dto, item, author);

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public List<CommentResponseDto> getCommentsForItem(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItem_Id(itemId);
        return comments
                .stream()
                .map(CommentMapper::toDto)
                .toList();
    }
}
