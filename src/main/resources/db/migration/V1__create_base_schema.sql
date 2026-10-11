-- ============================================
-- SpaceBook Database Schema - V1
-- ============================================

-- Users table (authentication base)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('VENDOR', 'ORGANIZER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Vendor profiles
CREATE TABLE vendor_profiles (
    user_id BIGINT PRIMARY KEY,
    business_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    category VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    description TEXT,
    website VARCHAR(500),
    years_operating INT,
    avatar_url VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category (category),
    INDEX idx_location (city, country)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Organizer profiles
CREATE TABLE organizer_profiles (
    user_id BIGINT PRIMARY KEY,
    organization_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    org_type ENUM('COLLEGE', 'COMPANY', 'NGO', 'INDIVIDUAL') NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    website VARCHAR(500),
    description TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_org_type (org_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh tokens (httpOnly cookie storage)
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_expires (user_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Events
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizer_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    venue_name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    currency CHAR(3) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'CLOSED', 'CANCELLED', 'COMPLETED') DEFAULT 'DRAFT',
    layout_type ENUM('GRID_HALL', 'OPEN_GROUND', 'STREET_MARKET', 'MULTI_ZONE') NOT NULL,
    booking_window_start TIMESTAMP NULL,
    booking_window_end TIMESTAMP NULL,
    max_stalls_per_vendor INT DEFAULT 5,
    approval_mode ENUM('MANUAL', 'AUTO') DEFAULT 'MANUAL',
    allowed_categories JSON,
    rules TEXT,
    cover_colour VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_organizer (organizer_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_location (city, country),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Event zones
CREATE TABLE event_zones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    allowed_category VARCHAR(100),
    base_price DECIMAL(10, 2) NOT NULL,
    colour VARCHAR(7),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Stall spaces
CREATE TABLE stall_spaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    stall_number VARCHAR(50) NOT NULL,
    zone_id BIGINT,
    width DECIMAL(5, 2) NOT NULL,
    depth DECIMAL(5, 2) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    facilities JSON,
    grid_row INT,
    grid_col INT,
    notes TEXT,
    version INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (zone_id) REFERENCES event_zones(id) ON DELETE SET NULL,
    UNIQUE KEY uk_event_stall (event_id, stall_number),
    INDEX idx_event (event_id),
    INDEX idx_category (category),
    INDEX idx_grid (event_id, grid_row, grid_col)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bookings
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stall_id BIGINT NOT NULL,
    vendor_id BIGINT NOT NULL,
    status ENUM('REQUESTED', 'APPROVED', 'REJECTED', 'CANCELLED') DEFAULT 'REQUESTED',
    message TEXT,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (stall_id) REFERENCES stall_spaces(id) ON DELETE CASCADE,
    FOREIGN KEY (vendor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_stall (stall_id),
    INDEX idx_vendor (vendor_id),
    INDEX idx_status (status),
    INDEX idx_stall_status (stall_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- CRITICAL: Conflict-free booking guarantee
-- ============================================
-- This table enforces at the database level that a stall
-- can have only ONE active allocation at any time.
-- The UNIQUE(stall_id) constraint makes a second allocation
-- for the same stall impossible - the INSERT will fail.
CREATE TABLE active_stall_allocations (
    stall_id BIGINT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stall_id) REFERENCES stall_spaces(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit logs
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSON,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_actor (actor_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (user_id, is_read),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Favourite events (for vendors)
CREATE TABLE favourite_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_event (user_id, event_id),
    INDEX idx_user (user_id),
    INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
