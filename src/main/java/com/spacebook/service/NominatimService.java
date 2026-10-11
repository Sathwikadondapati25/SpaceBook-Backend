package com.spacebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Nominatim proxy service for geocoding
 * Respects Nominatim usage policy: 1 request per second, caching, proper User-Agent
 */
@Service
public class NominatimService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NominatimService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Value("${spacebook.nominatim.base-url}")
    private String baseUrl;

    @Value("${spacebook.nominatim.user-agent}")
    private String userAgent;

    @Value("${spacebook.nominatim.rate-limit-ms}")
    private long rateLimitMs;

    private long lastRequestTime = 0;

    /**
     * Search for a place and return coordinates
     * Results are cached for 24 hours to respect Nominatim policy
     */
    @Cacheable(value = "nominatim", key = "#query", unless = "#result == null")
    public GeocodingResult search(String query) {
        // Rate limiting: wait if last request was too recent
        enforceRateLimit();

        try {
            String url = String.format("%s/search?format=json&q=%s&limit=1", baseUrl, 
                    java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));

            String response = webClient
                    .get()
                    .uri(url)
                    .header("User-Agent", userAgent)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            if (response == null || response.isEmpty()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            if (root.isArray() && root.size() > 0) {
                JsonNode first = root.get(0);
                JsonNode latNode = first.get("lat");
                JsonNode lonNode = first.get("lon");
                JsonNode displayNameNode = first.get("display_name");

                if (latNode != null && lonNode != null) {
                    return GeocodingResult.builder()
                            .latitude(new BigDecimal(latNode.asText()))
                            .longitude(new BigDecimal(lonNode.asText()))
                            .displayName(displayNameNode != null ? displayNameNode.asText() : query)
                            .build();
                }
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to geocode address: " + e.getMessage(), e);
        }
    }

    /**
     * Reverse geocode: get address from coordinates
     * Results are cached for 24 hours
     */
    @Cacheable(value = "nominatim", key = "'reverse_' + #lat + '_' + #lon", unless = "#result == null")
    public String reverseGeocode(BigDecimal lat, BigDecimal lon) {
        // Rate limiting
        enforceRateLimit();

        try {
            String url = String.format("%s/reverse?format=json&lat=%s&lon=%s", 
                    baseUrl, lat, lon);

            String response = webClient
                    .get()
                    .uri(url)
                    .header("User-Agent", userAgent)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            if (response == null || response.isEmpty()) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode displayNameNode = root.get("display_name");

            return displayNameNode != null ? displayNameNode.asText() : null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to reverse geocode: " + e.getMessage(), e);
        }
    }

    private void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;

        if (timeSinceLastRequest < rateLimitMs) {
            try {
                Thread.sleep(rateLimitMs - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }

    public static class GeocodingResult {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String displayName;

        public GeocodingResult() {}

        public GeocodingResult(BigDecimal latitude, BigDecimal longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }

        public BigDecimal getLatitude() { return latitude; }
        public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

        public BigDecimal getLongitude() { return longitude; }
        public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private BigDecimal latitude;
            private BigDecimal longitude;
            private String displayName;

            public Builder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
            public Builder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
            public Builder displayName(String displayName) { this.displayName = displayName; return this; }

            public GeocodingResult build() {
                return new GeocodingResult(latitude, longitude, displayName);
            }
        }
    }
}
