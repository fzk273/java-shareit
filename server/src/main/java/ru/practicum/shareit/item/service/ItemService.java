package ru.practicum.shareit.item.service;

import jakarta.validation.Valid;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
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

    List<CommentResponseDto> getCommentsForItem(Long itemId);

    CommentResponseDto createComment(Long userId, Long itemId, @Valid CommentCreateRequestDto dto);
}
