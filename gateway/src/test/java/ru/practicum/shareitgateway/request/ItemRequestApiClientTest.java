package ru.practicum.shareitgateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.request.dto.request.ItemRequestCreateDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(ItemRequestClient.class)
@TestPropertySource(properties = {
        "shareit.server.url=http://localhost:9090"
})
class ItemRequestApiClientTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private ItemRequestClient itemRequestClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_sendsPostWithUserHeaderAndBody() throws Exception {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description("need a drill")
                .build();

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_HEADER, "5"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":1,\"description\":\"need a drill\"}"));

        var response = itemRequestClient.createRequest(5L, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRequestsByUser_sendsGetWithUserHeader() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "7"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":1},{\"id\":2}]"));

        var response = itemRequestClient.getRequestsByUser(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getAllRequests_sendsGetWithQueryParams() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/requests/all?from=0&size=10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "9"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":10}]"));

        var response = itemRequestClient.getAllRequests(9L, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRequestById_sendsGetWithUserHeader() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/requests/42"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "3"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":42,\"description\":\"some request\"}"));

        var response = itemRequestClient.getRequestById(3L, 42L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRequestById_notFound_propagatesStatusAndBody() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/requests/999"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_HEADER, "3"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"request not found\"}"));

        var response = itemRequestClient.getRequestById(3L, 999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        mockServer.verify();
    }
}
