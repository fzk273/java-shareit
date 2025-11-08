package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.request.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ResponseEntity<ItemRequestResponseDto> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @RequestBody @Valid ItemRequestCreateDto dto) {

        ItemRequestResponseDto created = requestService.create(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestResponseWithItemsDto>> getRequestsByUserId(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequestResponseWithItemsDto> requests = requestService.getRequestsByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestResponseWithItemsDto>> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        List<ItemRequestResponseWithItemsDto> requests = requestService.getAll(userId, from, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestResponseWithItemsDto> getRequestById(@PathVariable Long requestId) {
        ItemRequestResponseWithItemsDto request = requestService.getRequestById(requestId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(request);
    }
}