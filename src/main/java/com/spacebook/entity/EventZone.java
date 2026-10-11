package com.spacebook.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Zone within an event with pricing and category restrictions
 */
@Entity
@Table(name = "event_zones")
public class EventZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "allowed_category", length = 100)
    private String allowedCategory;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 7)
    private String colour;
}
