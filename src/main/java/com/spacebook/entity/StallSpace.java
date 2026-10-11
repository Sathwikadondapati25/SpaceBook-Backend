package com.spacebook.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Individual stall space with grid position and facilities
 * Uses optimistic locking via @Version
 */
@Entity
@Table(name = "stall_spaces")
public class StallSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "stall_number", nullable = false, length = 50)
    private String stallNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private EventZone zone;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal width;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal depth;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(columnDefinition = "JSON")
    private String facilities;

    @Column(name = "grid_row")
    private Integer gridRow;

    @Column(name = "grid_col")
    private Integer gridCol;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Version
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StallSpace() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getStallNumber() { return stallNumber; }
    public void setStallNumber(String stallNumber) { this.stallNumber = stallNumber; }

    public EventZone getZone() { return zone; }
    public void setZone(EventZone zone) { this.zone = zone; }

    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }

    public BigDecimal getDepth() { return depth; }
    public void setDepth(BigDecimal depth) { this.depth = depth; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public Integer getGridRow() { return gridRow; }
    public void setGridRow(Integer gridRow) { this.gridRow = gridRow; }

    public Integer getGridCol() { return gridCol; }
    public void setGridCol(Integer gridCol) { this.gridCol = gridCol; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Event event;
        private String stallNumber;
        private EventZone zone;
        private BigDecimal width;
        private BigDecimal depth;
        private BigDecimal price;
        private String category;
        private String facilities;
        private Integer gridRow;
        private Integer gridCol;
        private String notes;

        public Builder event(Event event) { this.event = event; return this; }
        public Builder stallNumber(String stallNumber) { this.stallNumber = stallNumber; return this; }
        public Builder zone(EventZone zone) { this.zone = zone; return this; }
        public Builder width(BigDecimal width) { this.width = width; return this; }
        public Builder depth(BigDecimal depth) { this.depth = depth; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder facilities(String facilities) { this.facilities = facilities; return this; }
        public Builder gridRow(Integer gridRow) { this.gridRow = gridRow; return this; }
        public Builder gridCol(Integer gridCol) { this.gridCol = gridCol; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public StallSpace build() {
            StallSpace stall = new StallSpace();
            stall.setEvent(this.event);
            stall.setStallNumber(this.stallNumber);
            stall.setZone(this.zone);
            stall.setWidth(this.width);
            stall.setDepth(this.depth);
            stall.setPrice(this.price);
            stall.setCategory(this.category);
            stall.setFacilities(this.facilities);
            stall.setGridRow(this.gridRow);
            stall.setGridCol(this.gridCol);
            stall.setNotes(this.notes);
            return stall;
        }
    }
}
