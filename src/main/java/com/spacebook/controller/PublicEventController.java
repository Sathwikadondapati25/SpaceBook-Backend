package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.entity.Event;
import com.spacebook.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public event controller for vendors to discover events
 */
@RestController
@RequestMapping("/api/public/events")
@Tag(name = "Public Events", description = "Public event discovery endpoints")
public class PublicEventController {

    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Get all published events")
    public ResponseEntity<ApiResponse<List<Event>>> getPublishedEvents() {
        List<Event> events = eventService.getPublishedEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<Event>> getEvent(@PathVariable Long eventId) {
        Event event = eventService.getEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
}
