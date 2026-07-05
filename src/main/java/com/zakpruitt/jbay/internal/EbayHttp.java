package com.zakpruitt.jbay.internal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zakpruitt.jbay.JbayException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/** HTTP + JSON plumbing. Not part of the public API. */
public final class EbayHttp {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** Authenticated GET. Returns null on 204 or an empty body. */
    public <T> T get(String url, String bearerToken, Class<T> responseType) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Accept", "application/json")
                .GET()
                .build();
        return send(request, responseType);
    }

    /** Form-encoded POST with HTTP Basic auth — the shape of every eBay OAuth token call. */
    public <T> T postForm(String url, String username, String password, Map<String, String> form, Class<T> responseType) {
        String body = form.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        String basic = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(request, responseType);
    }

    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> T send(HttpRequest request, Class<T> responseType) {
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new JbayException("Request to " + request.uri() + " failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JbayException("Request to " + request.uri() + " was interrupted", e);
        }

        if (response.statusCode() >= 400) {
            throw new JbayException("eBay returned " + response.statusCode() + " for " + request.uri(),
                    response.statusCode(), response.body());
        }
        if (response.statusCode() == 204 || response.body() == null || response.body().isBlank()) {
            return null;
        }

        try {
            return mapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new JbayException("Could not parse eBay response from " + request.uri(), e);
        }
    }
}
