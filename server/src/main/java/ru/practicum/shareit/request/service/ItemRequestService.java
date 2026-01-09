package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.request.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItemRequestResponseDto create(Long userId, ItemRequestCreateDto dto) {
        User requester = getUserOrThrow(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(dto, requester);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestResponseDto(savedRequest);
    }

    public List<ItemRequestResponseWithItemsDto> getRequestsByUserId(Long id) {
        getUserOrThrow(id);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(id);
        return enrichWithItems(requests);
    }

    public ItemRequestResponseWithItemsDto getRequestById(Long id) {
        ItemRequest itemRequest = getItemRequestOrThrow(id);

        List<Item> items = itemRepository.findByItemRequest_Id(itemRequest.getId());

        return ItemRequestMapper.toItemRequestWithItemsDto(itemRequest, items);
    }

    public List<ItemRequestResponseWithItemsDto> getAll(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNot(userId, pageable).getContent();
        return enrichWithItems(requests);
    }

    private List<ItemRequestResponseWithItemsDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) return Collections.emptyList();

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByItemRequest_IdIn(requestIds);

        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(i -> i.getItemRequest().getId()));

        return requests.stream()
                .map(req -> {
                    List<Item> it = itemsByRequestId.getOrDefault(req.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestWithItemsDto(req, it);
                })
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
    }

    private ItemRequest getItemRequestOrThrow(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request not found with id " + id));
    }
}