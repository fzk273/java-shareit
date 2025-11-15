package ru.practicum.shareitgateway.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.item.comments.dto.request.CommentCreateRequestDto;
import ru.practicum.shareit.item.dto.request.ItemCreateDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(ItemApiClient.class)
@TestPropertySource(properties = {
        "shareit.server.url=http://localhost:9090"
})
class ItemApiClientTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ItemApiClient itemApiClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createItem_sendsPostWithUserHeaderAndBody() throws Exception {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("item")
                .description("desc")
                .available(true)
                .requestId(null)
                .build();

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_HEADER, "5"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1,\"name\":\"item\"}"));

        var response = itemApiClient.createItem(5L, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void updateItem_sendsPatchWithUserHeaderAndBody() {
        ItemUpdateDto dto = ItemUpdateDto.builder()
                .name("updated")
                .description("updated desc")
                .available(false)
                .build();

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items/10"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(USER_HEADER, "7"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":10,\"name\":\"updated\"}"));

        var response = itemApiClient.updateItem(7L, 10L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getById_sendsGetWithUserHeader() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items/3"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "9"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":3,\"name\":\"item3\"}"));

        var response = itemApiClient.getById(9L, 3L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getAllUserItems_sendsGetWithUserHeader() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "4"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":1},{\"id\":2}]"));

        var response = itemApiClient.getAllUserItems(4L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void searchItem_sendsGetWithQueryParam() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items/search?text=drill"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "6"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":5,\"name\":\"drill\"}]"));

        var response = itemApiClient.searchItem(6L, "drill");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void createComment_sendsPostToCommentEndpoint() {
        CommentCreateRequestDto dto = new CommentCreateRequestDto();
        dto.setText("nice item");

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items/10/comment"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_HEADER, "2"))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1,\"text\":\"nice item\"}"));

        var response = itemApiClient.createComment(2L, 10L, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getById_notFound_propagatesStatusAndBody() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/items/999"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "3"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"not found\"}"));

        var response = itemApiClient.getById(3L, 999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        mockServer.verify();
    }
}
