package ru.practicum.shareitgateway.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareitgateway.client.ApiClient;


@Service
public class UserApiClient extends ApiClient {
    private static final String PREFIX = "/users";

    public UserApiClient(RestTemplateBuilder restTemplate, @Value("${shareit.server.url}") String shareItServerUrl) {
        super(restTemplate
                .uriTemplateHandler(new DefaultUriBuilderFactory(shareItServerUrl + PREFIX))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getUsers() {
        return get("");
    }

    public ResponseEntity<Object> createUser(CreateUserDto dto) {
        return post("", dto);
    }

    public ResponseEntity<Object> updateUser(Long userId, UpdateUserDto dto) {
        return patch("/" + userId, dto);
    }

    public ResponseEntity<Object> deleteUser(Long id) {
        return delete("/" + id);
    }
}
