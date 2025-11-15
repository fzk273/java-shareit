package ru.practicum.shareitgateway.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareitgateway.client.ApiClient;

import java.util.Map;

@Service
public class ItemApiClient extends ApiClient {
    private static final String PREFIX = "/items";

    public ItemApiClient(RestTemplateBuilder restTemplate,
                         @Value("${shareit.server.url}") String shareItServerUrl) {
        super(restTemplate
                .uriTemplateHandler(new DefaultUriBuilderFactory(shareItServerUrl + PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createItem(Long userId, ItemCreateDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemUpdateDto dto) {
        return patch("/" + itemId, userId, dto);
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllUserItems(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> searchItem(Long userId, String text) {
        Map<String, Object> params = Map.of("text", text);
        return get("/search?text={text}", userId, params);
    }

    public ResponseEntity<Object> createComment(Long userId, Long itemId,
                                                @Valid CommentCreateRequestDto dto) {
        return post("/" + itemId + "/comment", userId, dto);
    }
}
