package com.spacebook.controller;

import com.spacebook.dto.ApiResponse;
import com.spacebook.dto.event.EventRequest;
import com.spacebook.entity.Event;
import com.spacebook.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Event controller for organizers
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Create new event")
    public ResponseEntity<ApiResponse<Event>> createEvent(@Valid @RequestBody EventRequest request,
                                                           Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        Event event = eventService.createEvent(organizerId, request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Update event")
    public ResponseEntity<ApiResponse<Event>> updateEvent(@PathVariable Long eventId,
                                                           @Valid @RequestBody EventRequest request,
                                                           Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        Event event = eventService.updateEvent(organizerId, eventId, request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID")
    public ResponseEntity<ApiResponse<Event>> getEvent(@PathVariable Long eventId) {
        Event event = eventService.getEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get organizer's events")
    public ResponseEntity<ApiResponse<List<Event>>> getMyEvents(Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        List<Event> events = eventService.getOrganizerEvents(organizerId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Delete event")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long eventId,
                                                           Authentication authentication) {
        Long organizerId = (Long) authentication.getPrincipal();
        eventService.deleteEvent(organizerId, eventId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
