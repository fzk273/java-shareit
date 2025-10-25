package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto createItem(Long userId, ItemCreateDto dto);

    ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto dto);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> getAllUserItems(Long userId);

    List<ItemResponseDto> searchItem(Long userId, String text);

}
