package ru.practicum.shareit.requestTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.request.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.response.ItemRequestResponseWithItemsDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_success() throws Exception {
        ItemRequestCreateDto req = ItemRequestCreateDto.builder()
                .description("need a drill")
                .build();

        ItemRequestResponseDto resp = ItemRequestResponseDto.builder()
                .id(1L)
                .description("need a drill")
                .created(LocalDateTime.now())
                .build();

        given(requestService.create(eq(2L), any(ItemRequestCreateDto.class))).willReturn(resp);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("need a drill"));
    }


    @Test
    void getRequestsByUserId_success() throws Exception {
        ItemRequestResponseWithItemsDto r1 = ItemRequestResponseWithItemsDto.builder()
                .id(10L).description("desc1").created(LocalDateTime.now()).items(List.of()).build();
        ItemRequestResponseWithItemsDto r2 = ItemRequestResponseWithItemsDto.builder()
                .id(11L).description("desc2").created(LocalDateTime.now()).items(List.of()).build();

        given(requestService.getRequestsByUserId(3L)).willReturn(List.of(r1, r2));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    @Test
    void getAllRequests_success_withPaging() throws Exception {
        ItemRequestResponseWithItemsDto r = ItemRequestResponseWithItemsDto.builder()
                .id(5L).description("other users").created(LocalDateTime.now()).items(List.of()).build();

        given(requestService.getAll(4L, 0, 2)).willReturn(List.of(r));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 4L)
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5));
    }


    @Test
    void getRequestById_success() throws Exception {
        ItemRequestResponseWithItemsDto r = ItemRequestResponseWithItemsDto.builder()
                .id(7L).description("single").created(LocalDateTime.now()).items(List.of()).build();

        given(requestService.getRequestById(7L)).willReturn(r);

        mockMvc.perform(get("/requests/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value("single"));
    }
}
