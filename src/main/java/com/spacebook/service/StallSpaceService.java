package com.spacebook.service;

import com.spacebook.dto.stall.StallSpaceRequest;
import com.spacebook.entity.Event;
import com.spacebook.entity.EventZone;
import com.spacebook.entity.StallSpace;
import com.spacebook.exception.BadRequestException;
import com.spacebook.exception.ConflictException;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.EventRepository;
import com.spacebook.repository.EventZoneRepository;
import com.spacebook.repository.StallSpaceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Stall space service for CRUD operations
 */
@Service
public class StallSpaceService {

    private final StallSpaceRepository stallSpaceRepository;
    private final EventRepository eventRepository;
    private final EventZoneRepository eventZoneRepository;
    private final ObjectMapper objectMapper;

    public StallSpaceService(StallSpaceRepository stallSpaceRepository,
                             EventRepository eventRepository,
                             EventZoneRepository eventZoneRepository,
                             ObjectMapper objectMapper) {
        this.stallSpaceRepository = stallSpaceRepository;
        this.eventRepository = eventRepository;
        this.eventZoneRepository = eventZoneRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StallSpace createStallSpace(Long organizerId, Long eventId, StallSpaceRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only add stalls to your own events");
        }

        // Check if stall number already exists for this event
        if (stallSpaceRepository.findByEventAndStallNumber(event, request.getStallNumber()).isPresent()) {
            throw new ConflictException("Stall number already exists for this event");
        }

        EventZone zone = null;
        if (request.getZoneId() != null) {
            zone = eventZoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));
        }

        StallSpace stall = StallSpace.builder()
                .event(event)
                .stallNumber(request.getStallNumber())
                .zone(zone)
                .width(request.getWidth())
                .depth(request.getDepth())
                .price(request.getPrice())
                .category(request.getCategory())
                .facilities(convertToJson(request.getFacilities()))
                .gridRow(request.getGridRow())
                .gridCol(request.getGridCol())
                .notes(request.getNotes())
                .build();

        return stallSpaceRepository.save(stall);
    }

    @Transactional
    public StallSpace updateStallSpace(Long organizerId, Long stallId, StallSpaceRequest request) {
        StallSpace stall = stallSpaceRepository.findById(stallId)
                .orElseThrow(() -> new ResourceNotFoundException("Stall not found"));

        if (!stall.getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only update your own stalls");
        }

        // Check if stall number conflicts with another stall
        stallSpaceRepository.findByEventAndStallNumber(stall.getEvent(), request.getStallNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(stallId)) {
                        throw new ConflictException("Stall number already exists for this event");
                    }
                });

        EventZone zone = null;
        if (request.getZoneId() != null) {
            zone = eventZoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found"));
        }

        stall.setStallNumber(request.getStallNumber());
        stall.setZone(zone);
        stall.setWidth(request.getWidth());
        stall.setDepth(request.getDepth());
        stall.setPrice(request.getPrice());
        stall.setCategory(request.getCategory());
        stall.setFacilities(convertToJson(request.getFacilities()));
        stall.setGridRow(request.getGridRow());
        stall.setGridCol(request.getGridCol());
        stall.setNotes(request.getNotes());

        return stallSpaceRepository.save(stall);
    }

    @Transactional(readOnly = true)
    public StallSpace getStallSpace(Long stallId) {
        return stallSpaceRepository.findById(stallId)
                .orElseThrow(() -> new ResourceNotFoundException("Stall not found"));
    }

    @Transactional(readOnly = true)
    public List<StallSpace> getEventStalls(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return stallSpaceRepository.findByEvent(event);
    }

    @Transactional
    public void deleteStallSpace(Long organizerId, Long stallId) {
        StallSpace stall = stallSpaceRepository.findById(stallId)
                .orElseThrow(() -> new ResourceNotFoundException("Stall not found"));

        if (!stall.getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only delete your own stalls");
        }

        stallSpaceRepository.delete(stall);
    }

    private String convertToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON format for facilities");
        }
    }
}
