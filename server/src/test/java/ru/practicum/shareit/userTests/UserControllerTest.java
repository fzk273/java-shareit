package ru.practicum.shareit.userTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.service.UserDbService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean()
    private UserDbService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUsers_success() throws Exception {
        List<UserResponseDto> list = List.of(
                UserResponseDto.builder().id(1L).name("u1").email("u1@mail.com").build(),
                UserResponseDto.builder().id(2L).name("u2").email("u2@mail.com").build()
        );
        given(userService.getUsers()).willReturn(list);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].email").value("u2@mail.com"));
    }

    @Test
    void getUserById_success() throws Exception {
        UserResponseDto dto = UserResponseDto.builder().id(10L).name("john").email("j@d.com").build();
        given(userService.getUserById(10L)).willReturn(dto);

        mockMvc.perform(get("/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("john"))
                .andExpect(jsonPath("$.email").value("j@d.com"));
    }

    @Test
    void createUser_success() throws Exception {
        CreateUserDto req = new CreateUserDto();
        req.setName("alex");
        req.setEmail("a@a.com");

        UserResponseDto resp = UserResponseDto.builder().id(5L).name("alex").email("a@a.com").build();
        given(userService.createUser(any(CreateUserDto.class))).willReturn(resp);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("alex"));
    }


    @Test
    void updateUser_success() throws Exception {
        UpdateUserDto req = new UpdateUserDto();
        req.setName("new name");
        req.setEmail("new@mail.com");

        UserResponseDto resp = UserResponseDto.builder().id(3L).name("new name").email("new@mail.com").build();
        given(userService.updateUser(eq(3L), any(UpdateUserDto.class))).willReturn(resp);

        mockMvc.perform(patch("/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("new name"))
                .andExpect(jsonPath("$.email").value("new@mail.com"));
    }

    @Test
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/users/9"))
                .andExpect(status().isOk());
    }
}
