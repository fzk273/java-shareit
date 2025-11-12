package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.comments.dto.CommentMapper;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemMapper {
    public static Item itemCreateRequestToEntity(ItemCreateDto dto) {
        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();
    }

    public static Item itemCreateRequestToEntity(ItemCreateDto dto, ItemRequest request) {
        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();

        item.setItemRequest(request);
        return item;
    }

    public static Item itemUpdateRequestToEntity(ItemUpdateDto dto) {
        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .build();
    }

    public static ItemResponseDto itemToResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static ItemResponseDto itemToResponseDtoWithAllFields(Item item,
                                                                 Long lastBookingId,
                                                                 Long nextBookingId,
                                                                 List<Comment> comments) {
        List<CommentResponseDto> commentDtos = comments == null ? List.of()
                : comments.stream().map(CommentMapper::toDto).toList();

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBookingId)
                .nextBooking(nextBookingId)
                .comments(commentDtos)
                .build();
    }
}
