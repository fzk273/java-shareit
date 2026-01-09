package ru.practicum.shareitgateway.item;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemApiClient itemApiClient;

    public ItemController(ItemApiClient itemApiClient) {
        this.itemApiClient = itemApiClient;
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemCreateDto dto) {
        return itemApiClient.createItem(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody ItemUpdateDto dto) {
        return itemApiClient.updateItem(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        return itemApiClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemApiClient.getAllUserItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam("text") String text) {
        return itemApiClient.searchItem(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createCommentForItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long itemId,
                                                       @RequestBody @Valid CommentCreateRequestDto dto) {
        return itemApiClient.createComment(userId, itemId, dto);
    }
}

