package ru.practicum.shareit.request;


import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.request.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestCreateDto dto, User requester) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest savedRequest) {
        return ItemRequestResponseDto.builder()
                .id(savedRequest.getId())
                .description(savedRequest.getDescription())
                .created(savedRequest.getCreated())
                .build();
    }

    public static ItemRequestResponseWithItemsDto toItemRequestWithItemsDto(ItemRequest itemRequest, List<Item> items) {
        return ItemRequestResponseWithItemsDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .items(items.stream()
                        .map(ItemMapper::itemToResponseDto)
                        .collect(Collectors.toList()))

                .created(itemRequest.getCreated())
                .build();
    }

}