-- ============================================================
-- Faculty Event Management System — Sample Data
-- Sri Lankan Names & Context
-- MySQL 8.0+
-- Run AFTER faculty_ems_schema.sql
-- ============================================================

USE faculty_events;

-- ============================================================
-- 1. USERS
-- Passwords are BCrypt hashes of "Password@123"
-- ============================================================
INSERT INTO users (username, email, password, full_name, role, enabled) VALUES
-- Faculty Admins
('admina',  'niroshan.perera@faculty.lk',    '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Niroshan Perera',        'ADMIN',         TRUE),
('sanduni_admin',   'sanduni.fernando@faculty.lk',   '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Sanduni Fernando',       'ADMIN',         TRUE),

-- Society Admins
('society_admin',        'kasun.rajapaksa@faculty.lk',    '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Kasun Rajapaksa',        'SOCIETY_ADMIN', TRUE),
('dilini_media',    'dilini.wickramasinghe@fac.lk',  '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Dilini Wickramasinghe',  'SOCIETY_ADMIN', TRUE),
('tharindu_music',  'tharindu.bandara@faculty.lk',   '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Tharindu Bandara',       'SOCIETY_ADMIN', TRUE),
('nimasha_drama',   'nimasha.silva@faculty.lk',      '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Nimasha Silva',          'SOCIETY_ADMIN', TRUE),
('ruwan_robotics',  'ruwan.dissanayake@faculty.lk',  '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Ruwan Dissanayake',      'SOCIETY_ADMIN', TRUE),

-- Regular Members
('amaya_m',         'amaya.jayawardena@faculty.lk',  '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Amaya Jayawardena',      'MEMBER',        TRUE),
('chaminda_m',      'chaminda.kumara@faculty.lk',    '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Chaminda Kumara',        'MEMBER',        TRUE),
('thilini_m',       'thilini.madushani@faculty.lk',  '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Thilini Madushani',      'MEMBER',        TRUE),
('praveen_m',       'praveen.subramaniam@faculty.lk','$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Praveen Subramaniam',    'MEMBER',        TRUE),
('ishara_m',        'ishara.wijesekara@faculty.lk',  '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Ishara Wijesekara',      'MEMBER',        TRUE),
('sachini_m',       'sachini.rathnayake@faculty.lk', '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Sachini Rathnayake',     'MEMBER',        TRUE),
('dinesh_m',        'dinesh.herath@faculty.lk',      '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Dinesh Herath',          'MEMBER',        TRUE),
('kavindi_m',       'kavindi.liyanage@faculty.lk',   '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Kavindi Liyanage',       'MEMBER',        TRUE),
('nuwan_m',         'nuwan.gunawardena@faculty.lk',  '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Nuwan Gunawardena',      'MEMBER',        TRUE),
('hiruni_m',        'hiruni.pathirana@faculty.lk',   '$2a$12$K8Jx1kqZ9mLpQwErTyUiOeN2vBcXdFgHjKlMnPqRsTuVwYzAbCdEf', 'Hiruni Pathirana',       'MEMBER',        TRUE);

-- ============================================================
-- 2. SOCIETIES
-- admin_id references users: kasun=3, dilini=4, tharindu=5, nimasha=6, ruwan=7
-- ============================================================
INSERT INTO societies (name, description, contact_email, logo_url, admin_id, active) VALUES
('IEEE Student Branch',
 'The IEEE Student Branch promotes engineering excellence and innovation among undergraduates through workshops, seminars, and industry connections.',
 'ieee@faculty.lk', '/logos/ieee.png', 163, TRUE),

('Media & Photography Club',
 'Dedicated to visual storytelling, journalism, videography, and digital content creation for faculty events and publications.',
 'media@faculty.lk', '/logos/media.png', 163, TRUE),

('Music Society',
 'Celebrating Sri Lankan and international music through performances, jam sessions, and annual concerts.',
 'music@faculty.lk', '/logos/music.png', 209, TRUE),

('Drama & Arts Society',
 'Nurturing creative talent in theatre, dance, art, and cultural performances reflecting Sri Lankan heritage.',
 'drama@faculty.lk', '/logos/drama.png', 209, TRUE),

('Robotics & AI Club',
 'Exploring the frontiers of robotics, artificial intelligence, and automation through hands-on projects and competitions.',
 'robotics@faculty.lk', '/logos/robotics.png', 209, TRUE);

-- ============================================================
-- 3. VENUES
-- ============================================================
INSERT INTO venues (name, location, capacity, has_projector, has_ac, has_sound, description, active) VALUES
('Main Auditorium',      'Block A – Ground Floor',  400, TRUE,  TRUE,  TRUE,  'Large auditorium with stage, podium, and full AV system. Ideal for concerts and major events.', TRUE),
('Seminar Hall 01',      'Block B – 1st Floor',     120, TRUE,  TRUE,  TRUE,  'Conference-style hall with projector, whiteboard, and microphone system.',                       TRUE),
('Seminar Hall 02',      'Block B – 2nd Floor',      80, TRUE,  TRUE,  FALSE, 'Mid-size seminar room suitable for presentations and workshops.',                                TRUE),
('Computer Lab A',       'Block C – Ground Floor',   40, TRUE,  TRUE,  FALSE, '40-seat computer lab with high-speed internet and projection facility.',                         TRUE),
('Computer Lab B',       'Block C – 1st Floor',      40, TRUE,  TRUE,  FALSE, 'Identical setup to Lab A; used for parallel sessions.',                                          TRUE),
('Outdoor Amphitheatre', 'Central Courtyard',        250, FALSE, FALSE, TRUE,  'Open-air amphitheatre suitable for cultural shows and evening events.',                         TRUE),
('Board Room',           'Admin Block – 3rd Floor',   20, TRUE,  TRUE,  FALSE, 'Executive boardroom for committee meetings and panel discussions.',                              TRUE);

-- ============================================================
-- 4. EVENTS
-- society_id: IEEE=1, Media=2, Music=3, Drama=4, Robotics=5
-- organiser_id: kasun=3, dilini=4, tharindu=5, nimasha=6, ruwan=7
-- ============================================================
INSERT INTO events 
(title, description, event_type, society_id, organiser_id, expected_attendees, status) VALUES

-- IEEE (id = 11)
('Arduino Bootcamp 2025',
 'A two-day hands-on bootcamp covering Arduino programming, sensor integration, and IoT fundamentals for beginners.',
 'WORKSHOP', 11, 213, 35, 'PUBLISHED'),

('Industry Connect — Tech Careers in Sri Lanka',
 'Panel discussion featuring engineers sharing career insights.',
 'SEMINAR', 11, 213, 110, 'PUBLISHED'),

('IEEE Annual General Meeting 2025',
 'Yearly AGM to elect new committee members.',
 'OTHER', 11, 213, 18, 'PUBLISHED'),

-- Media Club (id = 12)
('Campus Photography Walk',
 'Guided photography tour around campus.',
 'WORKSHOP', 12, 213, 30, 'PUBLISHED'),

('Short Film Premiere Night',
 'Screening of student-produced short films.',
 'CULTURAL', 12, 213, 200, 'PUBLISHED'),

-- Music Society (id = 13)
('Unplugged Sessions — Volume 3',
 'Acoustic live music evening.',
 'CULTURAL', 13, 213, 230, 'PUBLISHED'),

('Music Theory Workshop',
 'Introduction to music theory.',
 'WORKSHOP', 13, 213, 40, 'PUBLISHED'),

-- Drama & Arts (id = 14)
('Sinhala Drama Night — "Giraa"',
 'Original Sinhala stage drama.',
 'CULTURAL', 14, 213, 380, 'PUBLISHED'),

('Traditional Dance Showcase',
 'Kandyan and classical dance performance.',
 'CULTURAL', 14, 213, 250, 'PUBLISHED'),

-- Robotics Club (id = 15)
('Line Follower Robot Competition',
 'Inter-faculty robotics competition.',
 'COMPETITION', 15, 213, 90, 'PUBLISHED'),

('Machine Learning Fundamentals',
 'Workshop on Python and ML basics.',
 'WORKSHOP', 15, 213, 38, 'DRAFT'),

('Robotics Club AGM',
 'Annual meeting and showcase.',
 'OTHER', 15, 213, 15, 'PUBLISHED');
-- ============================================================
-- 5. VENUE BOOKINGS
-- Approved, Pending, Rejected, and Cancelled examples
-- ============================================================
INSERT INTO venue_bookings
  (event_id, venue_id, society_id, requested_by, booking_date, start_time, end_time, status, admin_note, reviewed_by, reviewed_at)
VALUES

-- ★ APPROVED bookings
-- Arduino Bootcamp Day 1 → Computer Lab A
(1,  4, 1, 3, '2025-08-12', '08:30:00', '12:30:00', 'APPROVED',
 'Lab confirmed. Ensure all Arduinos are charged before session.', 1, '2025-07-20 09:15:00'),

-- Arduino Bootcamp Day 2 → Computer Lab A
(1,  4, 1, 3, '2025-08-13', '08:30:00', '12:30:00', 'APPROVED',
 'Day 2 confirmed.', 1, '2025-07-20 09:17:00'),

-- Industry Connect → Seminar Hall 01
(2,  2, 1, 3, '2025-08-22', '14:00:00', '17:00:00', 'APPROVED',
 'Microphone and projector to be tested one hour before start.', 1, '2025-07-25 10:00:00'),

-- IEEE AGM → Board Room
(3,  7, 1, 3, '2025-09-05', '15:00:00', '17:00:00', 'APPROVED',
 'Boardroom confirmed for AGM.', 2, '2025-08-01 11:30:00'),

-- Photography Walk → Seminar Hall 02 (briefing room)
(4,  3, 2, 4, '2025-08-15', '09:00:00', '10:30:00', 'APPROVED',
 'Approved for pre-walk briefing session.', 1, '2025-07-30 14:00:00'),

-- Short Film Premiere → Main Auditorium
(5,  1, 2, 4, '2025-09-12', '18:00:00', '21:00:00', 'APPROVED',
 'Full AV setup required. Coordinate with the tech team by 16:00.', 1, '2025-08-05 09:45:00'),

-- Unplugged Sessions → Outdoor Amphitheatre
(6,  6, 3, 5, '2025-08-30', '17:30:00', '20:30:00', 'APPROVED',
 'Sound system crew to set up by 15:30. Chairs for 230 pax.', 2, '2025-08-02 13:00:00'),

-- Music Theory Workshop → Seminar Hall 02
(7,  3, 3, 5, '2025-08-20', '10:00:00', '13:00:00', 'APPROVED',
 'Approved. Keyboard/piano arrangement to be confirmed with venue team.', 1, '2025-08-01 10:00:00'),

-- Sinhala Drama Night → Main Auditorium
(8,  1, 4, 6, '2025-09-20', '18:30:00', '21:30:00', 'APPROVED',
 'Stage setup permission granted. Rehearsal booking to be submitted separately.', 1, '2025-08-10 08:30:00'),

-- Traditional Dance Showcase → Outdoor Amphitheatre
(9,  6, 4, 6, '2025-10-04', '17:00:00', '20:00:00', 'APPROVED',
 'Confirmed. Stage lighting request forwarded to facilities team.', 2, '2025-08-15 11:00:00'),

-- Line Follower Competition → Seminar Hall 01
(10, 2, 5, 7, '2025-09-27', '08:00:00', '17:00:00', 'APPROVED',
 'Full-day booking confirmed. Tables to be rearranged for competition tracks.', 1, '2025-08-20 09:00:00'),

-- Robotics AGM → Board Room
(12, 7, 5, 7, '2025-09-10', '14:00:00', '16:00:00', 'APPROVED',
 'Boardroom confirmed.', 2, '2025-08-22 10:30:00'),


-- ★ PENDING bookings (awaiting admin review)
-- ML Workshop Day 1 → Computer Lab B
(11, 5, 5, 7, '2025-10-14', '09:00:00', '12:00:00', 'PENDING',
 NULL, NULL, NULL),

-- ML Workshop Day 2 → Computer Lab B
(11, 5, 5, 7, '2025-10-21', '09:00:00', '12:00:00', 'PENDING',
 NULL, NULL, NULL),

-- Drama rehearsal slot → Main Auditorium (for Sinhala Drama)
(8,  1, 4, 6, '2025-09-18', '14:00:00', '18:00:00', 'PENDING',
 NULL, NULL, NULL),

-- IEEE extra session → Seminar Hall 02
(2,  3, 1, 3, '2025-08-23', '09:00:00', '11:00:00', 'PENDING',
 NULL, NULL, NULL),


-- ★ REJECTED bookings
-- Music Society tried to book Main Auditorium same night as Drama (conflict)
(6,  1, 3, 5, '2025-09-20', '17:00:00', '20:00:00', 'REJECTED',
 'Rejected: Main Auditorium already booked for Drama Night on this date. Please choose an alternative venue or date.',
 1, '2025-08-11 09:00:00'),

-- Media Club tried to book Seminar Hall 01 — overlaps with Line Follower Competition
(5,  2, 2, 4, '2025-09-27', '10:00:00', '13:00:00', 'REJECTED',
 'Rejected: Seminar Hall 01 is reserved for the Robotics Line Follower Competition all day.',
 2, '2025-08-21 14:00:00'),


-- ★ CANCELLED booking
-- IEEE cancelled a duplicate seminar room request
(2,  3, 1, 3, '2025-08-22', '13:00:00', '16:00:00', 'CANCELLED',
 'Cancelled by society admin — duplicate request submitted in error.', NULL, NULL);

-- ============================================================
-- 6. NOTIFICATIONS
-- ============================================================
INSERT INTO notifications (user_id, message, type, is_read) VALUES
-- To kasun (IEEE admin) — booking approvals
(3, 'Your booking for Computer Lab A on 2025-08-12 (08:30–12:30) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(3, 'Your booking for Computer Lab A on 2025-08-13 (08:30–12:30) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(3, 'Your booking for Seminar Hall 01 on 2025-08-22 (14:00–17:00) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(3, 'Your booking for Board Room on 2025-09-05 (15:00–17:00) has been APPROVED.',      'BOOKING_APPROVED', FALSE),
(3, 'Reminder: Arduino Bootcamp starts tomorrow at 08:30 in Computer Lab A.',           'REMINDER',         FALSE),

-- To dilini (Media admin)
(4, 'Your booking for Seminar Hall 02 on 2025-08-15 (09:00–10:30) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(4, 'Your booking for Main Auditorium on 2025-09-12 (18:00–21:00) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(4, 'Your booking for Seminar Hall 01 on 2025-09-27 has been REJECTED: Hall reserved for Robotics competition.', 'BOOKING_REJECTED', FALSE),

-- To tharindu (Music admin)
(5, 'Your booking for Outdoor Amphitheatre on 2025-08-30 (17:30–20:30) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(5, 'Your booking for Seminar Hall 02 on 2025-08-20 (10:00–13:00) has been APPROVED.',       'BOOKING_APPROVED', TRUE),
(5, 'Your booking for Main Auditorium on 2025-09-20 was REJECTED: venue already booked for Drama Night.', 'BOOKING_REJECTED', TRUE),

-- To nimasha (Drama admin)
(6, 'Your booking for Main Auditorium on 2025-09-20 (18:30–21:30) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(6, 'Your booking for Outdoor Amphitheatre on 2025-10-04 has been APPROVED.',           'BOOKING_APPROVED', FALSE),
(6, 'Your rehearsal booking for Main Auditorium on 2025-09-18 is pending admin review.','GENERAL',          FALSE),

-- To ruwan (Robotics admin)
(7, 'Your booking for Seminar Hall 01 on 2025-09-27 (08:00–17:00) has been APPROVED.', 'BOOKING_APPROVED', TRUE),
(7, 'Your booking for Board Room on 2025-09-10 has been APPROVED.',                     'BOOKING_APPROVED', TRUE),
(7, 'Your booking for Computer Lab B on 2025-10-14 is pending admin review.',           'GENERAL',          FALSE),

-- General system notifications to admins
(1, 'New booking request received from Nimasha Silva (Drama & Arts Society) for Main Auditorium.', 'GENERAL', TRUE),
(1, 'New booking request received from Ruwan Dissanayake (Robotics & AI Club) for Computer Lab B.','GENERAL', FALSE),
(2, 'New booking request received from Tharindu Bandara (Music Society) for Outdoor Amphitheatre.', 'GENERAL', TRUE);

-- ============================================================
-- 7. SOCIETY MEMBERS
-- ============================================================
INSERT INTO society_members (society_id, user_id, role_in_society) VALUES
-- IEEE Student Branch (society 1)
(1, 3,  'ADMIN'),   -- Kasun Rajapaksa
(1, 8,  'MEMBER'),  -- Amaya Jayawardena
(1, 9,  'MEMBER'),  -- Chaminda Kumara
(1, 11, 'MEMBER'),  -- Praveen Subramaniam
(1, 14, 'MEMBER'),  -- Dinesh Herath

-- Media & Photography Club (society 2)
(2, 4,  'ADMIN'),   -- Dilini Wickramasinghe
(2, 8,  'MEMBER'),  -- Amaya Jayawardena
(2, 10, 'MEMBER'),  -- Thilini Madushani
(2, 15, 'MEMBER'),  -- Kavindi Liyanage

-- Music Society (society 3)
(3, 5,  'ADMIN'),   -- Tharindu Bandara
(3, 9,  'MEMBER'),  -- Chaminda Kumara
(3, 12, 'MEMBER'),  -- Ishara Wijesekara
(3, 16, 'MEMBER'),  -- Nuwan Gunawardena
(3, 17, 'MEMBER'),  -- Hiruni Pathirana

-- Drama & Arts Society (society 4)
(4, 6,  'ADMIN'),   -- Nimasha Silva
(4, 10, 'MEMBER'),  -- Thilini Madushani
(4, 13, 'MEMBER'),  -- Sachini Rathnayake
(4, 17, 'MEMBER'),  -- Hiruni Pathirana

-- Robotics & AI Club (society 5)
(5, 7,  'ADMIN'),   -- Ruwan Dissanayake
(5, 11, 'MEMBER'),  -- Praveen Subramaniam
(5, 14, 'MEMBER'),  -- Dinesh Herath
(5, 15, 'MEMBER'),  -- Kavindi Liyanage
(5, 16, 'MEMBER');  -- Nuwan Gunawardena

-- ============================================================
-- QUICK VERIFICATION QUERIES
-- ============================================================
/*
-- Check user count per role
SELECT role, COUNT(*) AS total FROM users GROUP BY role;

-- Check bookings per status
SELECT status, COUNT(*) AS total FROM venue_bookings GROUP BY status;

-- Check unread notifications per user
SELECT u.full_name, COUNT(n.id) AS unread
FROM notifications n JOIN users u ON n.user_id = u.id
WHERE n.is_read = FALSE GROUP BY u.full_name;

-- Run conflict detection for a hypothetical slot
SELECT COUNT(*) AS conflicts FROM venue_bookings
WHERE  venue_id     = 1
AND    booking_date = '2025-09-20'
AND    status       = 'APPROVED'
AND    start_time   < '21:30:00'
AND    end_time     > '18:30:00';
-- Expected: 1 (Drama Night is already approved for that slot)
*/
