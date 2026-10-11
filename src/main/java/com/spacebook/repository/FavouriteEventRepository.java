package com.spacebook.repository;

import com.spacebook.entity.Event;
import com.spacebook.entity.FavouriteEvent;
import com.spacebook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteEventRepository extends JpaRepository<FavouriteEvent, Long> {

    List<FavouriteEvent> findByUser(User user);

    Optional<FavouriteEvent> findByUserAndEvent(User user, Event event);

    boolean existsByUserAndEvent(User user, Event event);

    void deleteByUserAndEvent(User user, Event event);
}
