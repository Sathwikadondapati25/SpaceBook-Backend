package com.spacebook.service;

import com.spacebook.dto.event.EventRequest;
import com.spacebook.entity.Event;
import com.spacebook.entity.Event.EventStatus;
import com.spacebook.entity.User;
import com.spacebook.exception.BadRequestException;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.EventRepository;
import com.spacebook.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Event service for CRUD operations
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public EventService(EventRepository eventRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Event createEvent(Long organizerId, EventRequest request) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (organizer.getRole() != User.Role.ORGANIZER) {
            throw new BadRequestException("Only organizers can create events");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Event event = Event.builder()
                .organizer(organizer)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .venueName(request.getVenueName())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .timezone(request.getTimezone())
                .currency(request.getCurrency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : EventStatus.DRAFT)
                .layoutType(request.getLayoutType())
                .bookingWindowStart(request.getBookingWindowStart())
                .bookingWindowEnd(request.getBookingWindowEnd())
                .maxStallsPerVendor(request.getMaxStallsPerVendor() != null ? request.getMaxStallsPerVendor() : 5)
                .approvalMode(request.getApprovalMode())
                .allowedCategories(convertToJson(request.getAllowedCategories()))
                .rules(request.getRules())
                .coverColour(request.getCoverColour())
                .build();

        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long organizerId, Long eventId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only update your own events");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setVenueName(request.getVenueName());
        event.setAddress(request.getAddress());
        event.setCity(request.getCity());
        event.setCountry(request.getCountry());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setTimezone(request.getTimezone());
        event.setCurrency(request.getCurrency());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setStatus(request.getStatus());
        event.setLayoutType(request.getLayoutType());
        event.setBookingWindowStart(request.getBookingWindowStart());
        event.setBookingWindowEnd(request.getBookingWindowEnd());
        event.setMaxStallsPerVendor(request.getMaxStallsPerVendor());
        event.setApprovalMode(request.getApprovalMode());
        event.setAllowedCategories(convertToJson(request.getAllowedCategories()));
        event.setRules(request.getRules());
        event.setCoverColour(request.getCoverColour());

        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    @Transactional(readOnly = true)
    public List<Event> getOrganizerEvents(Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return eventRepository.findByOrganizer(organizer);
    }

    @Transactional(readOnly = true)
    public List<Event> getPublishedEvents() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }

    @Transactional
    public void deleteEvent(Long organizerId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new BadRequestException("You can only delete your own events");
        }

        eventRepository.delete(event);
    }

    private String convertToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON format for allowed categories");
        }
    }
}
