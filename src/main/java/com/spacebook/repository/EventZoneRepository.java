package com.spacebook.repository;

import com.spacebook.entity.Event;
import com.spacebook.entity.EventZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventZoneRepository extends JpaRepository<EventZone, Long> {

    List<EventZone> findByEvent(Event event);
}
