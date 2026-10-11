package com.spacebook.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Favourite events for vendors
 */
@Entity
@Table(name = "favourite_events",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
public class FavouriteEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
