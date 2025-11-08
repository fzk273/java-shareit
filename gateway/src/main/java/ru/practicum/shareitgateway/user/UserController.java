package ru.practicum.shareitgateway.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;


@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserApiClient apiClient;

    public UserController(UserApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @GetMapping
    ResponseEntity<Object> getUsers() {
        return apiClient.getUsers();
    }

    @GetMapping("/{id}")
    ResponseEntity<Object> getUserById(@PathVariable("id") Long id) {
        return apiClient.getUserById(id);
    }

    @PostMapping()
    ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserDto dto) {
        return apiClient.createUser(dto);
    }

    @PatchMapping("/{id}")
    ResponseEntity<Object> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserDto dto) {
        return apiClient.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deleteUser(@PathVariable("id") Long id) {
        return apiClient.deleteUser(id);
    }
}
