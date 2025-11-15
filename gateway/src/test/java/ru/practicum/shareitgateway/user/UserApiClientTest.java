package ru.practicum.shareitgateway.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.user.dto.request.CreateUserDto;
import ru.practicum.shareit.user.dto.request.UpdateUserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(UserApiClient.class)
@TestPropertySource(properties = {
        "shareit.server.url=http://localhost:9090"
})
class UserApiClientTest {

    @Autowired
    private UserApiClient userApiClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUsers_sendsGetToUsers() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[{\"id\":1,\"name\":\"u1\"}]"));

        var response = userApiClient.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getUserById_sendsGetToUsersId() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/users/5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":5,\"name\":\"user5\"}"));

        var response = userApiClient.getUserById(5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void createUser_sendsPostWithBody() throws Exception {
        CreateUserDto dto = new CreateUserDto();
        dto.setName("new");
        dto.setEmail("new@mail.com");

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":10,\"name\":\"new\"}"));

        var response = userApiClient.createUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void updateUser_sendsPatchWithBody() {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setName("updated");
        dto.setEmail("updated@mail.com");

        mockServer.expect(once(),
                        requestTo("http://localhost:9090/users/3"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":3,\"name\":\"updated\"}"));

        var response = userApiClient.updateUser(3L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void deleteUser_sendsDelete() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/users/7"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK));

        var response = userApiClient.deleteUser(7L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getUserById_notFound_propagatesStatusAndBody() {
        mockServer.expect(once(),
                        requestTo("http://localhost:9090/users/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"user not found\"}"));

        var response = userApiClient.getUserById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        mockServer.verify();
    }
}
