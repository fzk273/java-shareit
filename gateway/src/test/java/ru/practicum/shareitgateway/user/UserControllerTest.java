package ru.practicum.shareitgateway.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserApiClient userApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUsers_returnsOkFromClient() throws Exception {
        given(userApiClient.getUsers()).willReturn(
                ResponseEntity.ok().body(
                        java.util.List.of(Map.of("id", 1, "name", "u1"))
                )
        );

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUserById_returnsOkFromClient() throws Exception {
        given(userApiClient.getUserById(5L)).willReturn(
                ResponseEntity.ok(Map.of("id", 5, "name", "u5"))
        );

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createUser_success() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setName("new");
        dto.setEmail("new@mail.com");

        given(userApiClient.createUser(any(CreateUserDto.class))).willReturn(
                ResponseEntity.status(201).body(Map.of("id", 10, "name", "new"))
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createUser_validationError_blankName() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setName("   "); // нарушаем @NotBlank
        dto.setEmail("mail@mail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_success() throws Exception {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("updated");
        dto.setEmail("updated@mail.com");

        given(userApiClient.updateUser(eq(3L), any(UpdateUserDto.class))).willReturn(
                ResponseEntity.ok(Map.of("id", 3, "name", "updated"))
        );

        mockMvc.perform(patch("/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void deleteUser_success() throws Exception {
        given(userApiClient.deleteUser(7L)).willReturn(
                ResponseEntity.ok().build()
        );

        mockMvc.perform(delete("/users/7"))
                .andExpect(status().isOk());
    }
}
