package ru.practicum.shareit.itemTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.comments.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemDbService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean()
    private ItemDbService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createItem_success() throws Exception {
        ItemCreateDto req = ItemCreateDto.builder()
                .name("item")
                .description("item desc")
                .available(true)
                .requestId(null)
                .build();

        ItemResponseDto resp = ItemResponseDto.builder()
                .id(1L)
                .name("item")
                .description("item desc")
                .available(true)
                .comments(null)
                .bookings(null)
                .lastBooking(null)
                .nextBooking(null)
                .build();

        given(itemService.createItem(eq(2L), any(ItemCreateDto.class))).willReturn(resp);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("item"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateItem_success() throws Exception {
        ItemUpdateDto req = ItemUpdateDto.builder()
                .name("updated")
                .description("updated desc")
                .available(false)
                .build();

        ItemResponseDto resp = ItemResponseDto.builder()
                .id(5L)
                .name("updated")
                .description("updated desc")
                .available(false)
                .comments(null)
                .bookings(null)
                .lastBooking(null)
                .nextBooking(null)
                .build();

        given(itemService.updateItem(eq(2L), eq(5L), any(ItemUpdateDto.class))).willReturn(resp);

        mockMvc.perform(patch("/items/5")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("updated"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItem_success() throws Exception {
        ItemResponseDto resp = ItemResponseDto.builder()
                .id(10L)
                .name("item10")
                .description("desc10")
                .available(true)
                .comments(List.of())
                .bookings(null)
                .lastBooking(null)
                .nextBooking(null)
                .build();

        given(itemService.getById(eq(3L), eq(10L))).willReturn(resp);

        mockMvc.perform(get("/items/10")
                        .header(USER_HEADER, 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("item10"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getAllUserItems_success() throws Exception {
        ItemResponseDto i1 = ItemResponseDto.builder()
                .id(1L).name("i1").description("d1").available(true).build();
        ItemResponseDto i2 = ItemResponseDto.builder()
                .id(2L).name("i2").description("d2").available(false).build();

        given(itemService.getAllUserItems(eq(7L))).willReturn(List.of(i1, i2));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void searchItems_success() throws Exception {
        ItemResponseDto i = ItemResponseDto.builder()
                .id(3L).name("hammer").description("steel").available(true).build();

        given(itemService.searchItem(eq(4L), eq("ham"))).willReturn(List.of(i));

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, 4L)
                        .param("text", "ham"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("hammer"));
    }

    @Test
    void createComment_success() throws Exception {
        CommentCreateRequestDto req = new CommentCreateRequestDto();
        req.setText("nice item");

        CommentResponseDto resp = CommentResponseDto.builder()
                .id(11L)
                .text("nice item")
                .authorName("owner")
                .created(null)
                .build();

        given(itemService.createComment(eq(2L), eq(5L), any(CommentCreateRequestDto.class))).willReturn(resp);

        mockMvc.perform(post("/items/5/comment")
                        .header(USER_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.text").value("nice item"))
                .andExpect(jsonPath("$.authorName").value("owner"));
    }
}
