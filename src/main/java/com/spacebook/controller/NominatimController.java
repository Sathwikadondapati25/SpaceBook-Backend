package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.service.NominatimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Nominatim proxy controller for geocoding
 */
@RestController
@RequestMapping("/api/geocoding")
@Tag(name = "Geocoding", description = "Geocoding endpoints")
public class NominatimController {

    private final NominatimService nominatimService;

    public NominatimController(NominatimService nominatimService) {
        this.nominatimService = nominatimService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search for a place and return coordinates")
    public ResponseEntity<ApiResponse<NominatimService.GeocodingResult>> search(@RequestParam String query) {
        NominatimService.GeocodingResult result = nominatimService.search(query);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/reverse")
    @Operation(summary = "Get address from coordinates")
    public ResponseEntity<ApiResponse<String>> reverseGeocode(@RequestParam BigDecimal lat,
                                                              @RequestParam BigDecimal lon) {
        String address = nominatimService.reverseGeocode(lat, lon);
        return ResponseEntity.ok(ApiResponse.success(address));
    }
}
