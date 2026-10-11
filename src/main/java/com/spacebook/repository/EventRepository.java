package com.spacebook.repository;

import com.spacebook.entity.Event;
import com.spacebook.entity.Event.EventStatus;
import com.spacebook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByStatusAndStartDateAfter(EventStatus status, LocalDateTime date);

    List<Event> findByCityAndCountryAndStatus(String city, String country, EventStatus status);

    boolean existsByOrganizerAndId(User organizer, Long id);
}
