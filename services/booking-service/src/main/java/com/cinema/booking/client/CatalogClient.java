package com.cinema.booking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CatalogClient {
    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    private final RestClient restClient;

    public CatalogClient(@Value("${catalog.base-url:http://localhost:8081}") String catalogBaseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl(catalogBaseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Validates that the referenced showtime exists in Catalog Service before accessing or reserving seats.
     */
    public void ensureShowtimeExists(String showtimeId) {
        try {
            restClient.get()
                    .uri("/api/v2/showtimes/{id}", showtimeId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime " + showtimeId + " not found");
        } catch (RestClientException ex) {
            log.warn("Catalog Service unavailable when validating showtimeId={}: {}", showtimeId, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalog service is unavailable");
        }
    }
}
