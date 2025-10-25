package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Qualifier("ItemDbService")
@Service
public class ItemDbService implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public ItemDbService(ItemRepository itemRepository,
                         @Qualifier("UserDbService") UserService userService,
                         UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public ItemResponseDto createItem(Long userId, ItemCreateDto dto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }
        User itemOwner = userRepository.getById(userId);
        Item item = ItemMapper.itemCreateRequestToEntity(dto);
        item.setOwner(itemOwner);
        itemRepository.save(item);
        return ItemMapper.itemToResponseDto(item);
    }

    @Transactional
    @Override
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto dto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }

        Item currentItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("There is no such item with id: " + itemId));

//         (опционально) проверка, что обновляет владелец
//         if (!currentItem.getOwner().getId().equals(userId)) {
//             throw new ForbiddenException("Item can be updated only by its owner");
//         }

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
        Item item = itemRepository.getById(itemId);
        return ItemMapper.itemToResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::itemToResponseDto)
                .toList();
    }

    @Override
    public List<ItemResponseDto> searchItem(Long userId, String text) {
        String safeString = text == null ? "" : text.trim();
        if (safeString.isEmpty()) return Collections.emptyList();

        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("There is no such user with id: " + userId);
        }
        List<Item> userItems = itemRepository.findByOwnerId(userId);
        if (userItems.isEmpty()) return Collections.emptyList();

        String safeStringLowerCase = safeString.toLowerCase(Locale.ROOT);

        return userItems.stream()
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
