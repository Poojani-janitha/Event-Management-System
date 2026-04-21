-- ============================================================
-- Faculty Event Management System — Database Schema
-- MySQL 8.0+
-- Version 1.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS faculty_events
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE faculty_events;

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  username    VARCHAR(50)  NOT NULL,
  email       VARCHAR(100) NOT NULL,
  password    VARCHAR(255) NOT NULL,           -- BCrypt hashed; NEVER plain text
  full_name   VARCHAR(100) NOT NULL,
  role        ENUM('ADMIN','SOCIETY_ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',
  enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP    NULL     ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uq_users_username (username),
  UNIQUE KEY uq_users_email    (email)
);

-- ============================================================
-- 2. SOCIETIES
-- ============================================================
CREATE TABLE societies (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  name          VARCHAR(100) NOT NULL,
  description   TEXT         NULL,
  contact_email VARCHAR(100) NOT NULL,
  logo_url      VARCHAR(255) NULL,
  admin_id      BIGINT       NOT NULL,          -- FK → users(id)
  active        BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uq_societies_name (name),
  CONSTRAINT fk_societies_admin
    FOREIGN KEY (admin_id) REFERENCES users (id)
);

-- ============================================================
-- 3. VENUES
-- ============================================================
CREATE TABLE venues (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  name          VARCHAR(100) NOT NULL,
  location      VARCHAR(150) NOT NULL,
  capacity      INT          NOT NULL,
  has_projector BOOLEAN      NOT NULL DEFAULT FALSE,
  has_ac        BOOLEAN      NOT NULL DEFAULT FALSE,
  has_sound     BOOLEAN      NOT NULL DEFAULT FALSE,
  description   TEXT         NULL,
  active        BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uq_venues_name (name),
  INDEX idx_venues_active (active)              -- speeds up availability queries
);

-- ============================================================
-- 4. EVENTS
-- ============================================================
CREATE TABLE events (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  title              VARCHAR(150) NOT NULL,
  description        TEXT         NULL,
  event_type         ENUM('WORKSHOP','SEMINAR','COMPETITION','CULTURAL','OTHER') NOT NULL,
  society_id         BIGINT       NOT NULL,     -- FK → societies(id)
  organiser_id       BIGINT       NOT NULL,     -- FK → users(id)
  expected_attendees INT          NOT NULL,
  status             ENUM('DRAFT','PUBLISHED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'DRAFT',
  created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP    NULL     ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  CONSTRAINT fk_events_society
    FOREIGN KEY (society_id)   REFERENCES societies (id),
  CONSTRAINT fk_events_organiser
    FOREIGN KEY (organiser_id) REFERENCES users     (id)
);

-- ============================================================
-- 5. VENUE BOOKINGS  ★  Conflict-Prevention Key Table
-- ============================================================
CREATE TABLE venue_bookings (
  id           BIGINT    NOT NULL AUTO_INCREMENT,
  event_id     BIGINT    NOT NULL,              -- FK → events(id)
  venue_id     BIGINT    NOT NULL,              -- FK → venues(id)
  society_id   BIGINT    NOT NULL,              -- FK → societies(id)
  requested_by BIGINT    NOT NULL,              -- FK → users(id)
  booking_date DATE      NOT NULL,
  start_time   TIME      NOT NULL,
  end_time     TIME      NOT NULL,
  status       ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  admin_note   TEXT      NULL,
  reviewed_by  BIGINT    NULL,                  -- FK → users(id)
  reviewed_at  TIMESTAMP NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  CONSTRAINT fk_vb_event
    FOREIGN KEY (event_id)     REFERENCES events    (id),
  CONSTRAINT fk_vb_venue
    FOREIGN KEY (venue_id)     REFERENCES venues    (id),
  CONSTRAINT fk_vb_society
    FOREIGN KEY (society_id)   REFERENCES societies (id),
  CONSTRAINT fk_vb_requested_by
    FOREIGN KEY (requested_by) REFERENCES users     (id),
  CONSTRAINT fk_vb_reviewed_by
    FOREIGN KEY (reviewed_by)  REFERENCES users     (id),

  -- Composite index covering all columns used in conflict detection query
  INDEX idx_vb_conflict (venue_id, booking_date, status, start_time, end_time)
);

-- ============================================================
-- 6. NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
  id         BIGINT    NOT NULL AUTO_INCREMENT,
  user_id    BIGINT    NOT NULL,                -- FK → users(id)
  message    TEXT      NOT NULL,
  type       ENUM('BOOKING_APPROVED','BOOKING_REJECTED','REMINDER','GENERAL') NOT NULL,
  is_read    BOOLEAN   NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  CONSTRAINT fk_notif_user
    FOREIGN KEY (user_id) REFERENCES users (id),
  INDEX idx_notif_user_unread (user_id, is_read)  -- fast unread poll on dashboard
);

-- ============================================================
-- 7. SOCIETY MEMBERS
-- ============================================================
CREATE TABLE society_members (
  id              BIGINT    NOT NULL AUTO_INCREMENT,
  society_id      BIGINT    NOT NULL,           -- FK → societies(id)
  user_id         BIGINT    NOT NULL,           -- FK → users(id)
  joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  role_in_society ENUM('ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',

  PRIMARY KEY (id),
  UNIQUE KEY uq_society_member (society_id, user_id),  -- prevents duplicate memberships
  CONSTRAINT fk_sm_society
    FOREIGN KEY (society_id) REFERENCES societies (id),
  CONSTRAINT fk_sm_user
    FOREIGN KEY (user_id)    REFERENCES users     (id)
);

-- ============================================================
-- CONFLICT DETECTION QUERY
-- Used by VenueBookingService / VenueBookingRepository.
-- Returns > 0  →  conflict exists for the requested slot.
-- Implements the interval-overlap formula:
--   A_start < B_end  AND  A_end > B_start
-- ============================================================
/*
SELECT COUNT(*)
FROM   venue_bookings
WHERE  venue_id     = :venueId
AND    booking_date = :date
AND    status       = 'APPROVED'
AND    start_time   < :endTime      -- overlap: existing starts before new ends
AND    end_time     > :startTime;   -- overlap: existing ends after new starts
*/

-- ============================================================
-- FIRST ADMIN SETUP
-- After registering the first account via the UI, promote it here:
--   UPDATE users SET role = 'ADMIN' WHERE username = 'your_username';
-- ============================================================
