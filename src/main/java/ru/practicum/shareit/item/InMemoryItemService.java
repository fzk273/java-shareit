package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InMemoryItemService implements ItemService {
    //userId: {itemId: item}
    private final HashMap<Long, HashMap<Long, Item>> items;
    private final UserService userService;
    private Long itemsCounter = 1L;


    @Override
    public ItemResponseDto createItem(Long userId, ItemCreateDto dto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }
        Item newItem = ItemMapper.itemCreateRequestToEntity(dto);
        newItem.setId(itemsCounter);
        items.computeIfAbsent(userId, k -> new HashMap<>()).put(itemsCounter, newItem);
        itemsCounter++;
        return ItemMapper.itemToResponseDto(newItem);
    }

    @Override
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto dto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }

        HashMap<Long, Item> userItems = items.get(userId);
        Item currentItem = userItems.get(itemId);
        if (currentItem == null) {
            throw new NotFoundException("There is no such item with id: " + userId);
        }

        Item updatedItem = ItemMapper.itemUpdateRequestToEntity(dto);
        if (updatedItem.getDescription() != null && !updatedItem.getDescription().isBlank()) {
            currentItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getName() != null && !updatedItem.getName().isBlank()) {
            currentItem.setName(updatedItem.getName());
        }
        if (updatedItem.getAvailable() != null) {
            currentItem.setAvailable(updatedItem.getAvailable());
        }
        userItems.replace(itemId, currentItem);

        return ItemMapper.itemToResponseDto(currentItem);
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }
        HashMap<Long, Item> userItems = items.get(userId);
        Item item = userItems.get(itemId);
        if (item == null) {
            throw new NotFoundException("There is no such item with id: " + userId);
        }
        return ItemMapper.itemToResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }
        return items.get(userId).values().stream()
                .map(ItemMapper::itemToResponseDto).toList();
    }

    @Override
    public List<ItemResponseDto> searchItem(Long userId, String text) {
        String safeString = text == null ? "" : text.trim();
        if (safeString.isEmpty()) return Collections.emptyList();

        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }

        Map<Long, Item> userItems = items.getOrDefault(userId, new HashMap<>());
        if (userItems.isEmpty()) return Collections.emptyList();

        String safeStringLowerCase = safeString.toLowerCase(Locale.ROOT);

        return userItems.values().stream()
                .filter(item -> containsIgnoreCase(item.getName(), safeStringLowerCase)
                        || containsIgnoreCase(item.getDescription(), safeStringLowerCase))
                .filter(Item::getAvailable)
                .map(ItemMapper::itemToResponseDto)
                .toList();
    }

    private static boolean containsIgnoreCase(String field, String strLower) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(strLower);
    }
}
