package ru.practicum.shareitgateway.client;

import jakarta.annotation.Nullable;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ApiClient {
    protected final RestTemplate restTemplate;

    public ApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false; // не считаем ни 4xx, ни 5xx ошибками для RestTemplate
            }
        });
    }

    public ResponseEntity<Object> get(String path) {
        return sendRequestToApi(HttpMethod.GET, path, null, null, null);
    }

    public ResponseEntity<Object> get(String path, Long userId) {
        return sendRequestToApi(HttpMethod.GET, path, userId, null, null);
    }

    public ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return sendRequestToApi(HttpMethod.GET, path, userId, parameters, null);
    }

    public <T> ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        return sendRequestToApi(HttpMethod.GET, path, userId, parameters, body);
    }

    public <T> ResponseEntity<Object> post(String path, T body) {
        return sendRequestToApi(HttpMethod.POST, path, null, null, body);
    }

    public <T> ResponseEntity<Object> post(String path, Long userId, T body) {
        return sendRequestToApi(HttpMethod.POST, path, userId, null, body);
    }

    public <T> ResponseEntity<Object> post(String path, Long userId, Map<String, Object> parameters, T body) {
        return sendRequestToApi(HttpMethod.POST, path, userId, parameters, body);
    }

    public <T> ResponseEntity<Object> patch(String path, T body) {
        return patch(path, null, null, body);
    }

    public <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return patch(path, userId, null, body);
    }

    public <T> ResponseEntity<Object> patch(String path, Long userId, @Nullable Map<String, Object> parameters, T body) {
        return sendRequestToApi(HttpMethod.PATCH, path, userId, parameters, body);
    }

    public ResponseEntity<Object> delete(String path) {
        return delete(path, null, null);
    }

    public ResponseEntity<Object> delete(String path, long userId) {
        return delete(path, userId, null);
    }

    public ResponseEntity<Object> delete(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return sendRequestToApi(HttpMethod.DELETE, path, userId, parameters, null);
    }

    private <T> ResponseEntity<Object> sendRequestToApi(HttpMethod httpMethod, String path, Long userId,
                                                        @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }

        HttpEntity<T> request = new HttpEntity<>(body, headers);
        ResponseEntity<Object> response;
        try {
            if (parameters != null) {
                response = restTemplate.exchange(path, httpMethod, request, Object.class, parameters);
            } else {
                response = restTemplate.exchange(path, httpMethod, request, Object.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return gatewayResponse(response);
    }

    private static ResponseEntity<Object> gatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
