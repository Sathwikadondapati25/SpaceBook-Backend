package com.spacebook.dto.stall;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class StallSpaceRequest {

    @NotBlank(message = "Stall number is required")
    private String stallNumber;

    private Long zoneId;

    @NotNull(message = "Width is required")
    @Positive(message = "Width must be positive")
    private BigDecimal width;

    @NotNull(message = "Depth is required")
    @Positive(message = "Depth must be positive")
    private BigDecimal depth;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;

    private List<String> facilities;

    private Integer gridRow;

    private Integer gridCol;

    private String notes;

    public StallSpaceRequest() {
    }

    public String getStallNumber() { return stallNumber; }
    public void setStallNumber(String stallNumber) { this.stallNumber = stallNumber; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }

    public BigDecimal getDepth() { return depth; }
    public void setDepth(BigDecimal depth) { this.depth = depth; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }

    public Integer getGridRow() { return gridRow; }
    public void setGridRow(Integer gridRow) { this.gridRow = gridRow; }

    public Integer getGridCol() { return gridCol; }
    public void setGridCol(Integer gridCol) { this.gridCol = gridCol; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
