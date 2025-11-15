package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(@Qualifier("ItemDbService") ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestBody ItemCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createItem(userId, dto));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable("itemId") Long itemId,
                                                      @RequestBody ItemUpdateDto dto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.updateItem(userId, itemId, dto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable("itemId") Long itemId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.getById(userId, itemId));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.getAllUserItems(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @RequestParam("text") String text) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.searchItem(userId, text));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> createCommentForItem(
            @RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
            @RequestBody CommentCreateRequestDto dto) {

        CommentResponseDto commentDto = itemService.createComment(userId, itemId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentDto);
    }
}
