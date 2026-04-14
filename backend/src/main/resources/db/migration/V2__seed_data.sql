-- V2: Seed data

-- Locations
INSERT INTO locations (name, description, building, room) VALUES
    ('Control Room Alpha', 'Main operations control room', 'Building A', 'A-101'),
    ('Server Room B1', 'Primary computing infrastructure', 'Building B', 'B-001'),
    ('Detector Lab C3', 'Detector assembly and testing lab', 'Building C', 'C-300'),
    ('Electronics Workshop', 'Electronics repair and calibration', 'Building D', 'D-210'),
    ('Cryogenics Bay', 'Cryogenic systems maintenance bay', 'Building E', 'E-105')
ON CONFLICT (name) DO NOTHING;

-- Users (passwords are BCrypt of 'password123')
INSERT INTO users (username, email, password_hash, full_name, role, enabled) VALUES
    ('admin', 'admin@facility.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyxAi5MK2', 'System Administrator', 'ADMIN', TRUE),
    ('coordinator1', 'c.jones@facility.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyxAi5MK2', 'Carol Jones', 'COORDINATOR', TRUE),
    ('operator1', 'm.patel@facility.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyxAi5MK2', 'Mehul Patel', 'OPERATOR', TRUE),
    ('operator2', 's.chen@facility.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyxAi5MK2', 'Sophie Chen', 'OPERATOR', TRUE),
    ('viewer1', 'j.doe@facility.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyxAi5MK2', 'Jane Doe', 'VIEWER', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Resources
INSERT INTO resources (name, description, category, status, location_id, notes) VALUES
    ('Silicon Tracker Module A', 'Primary silicon strip tracking detector module', 'DETECTOR', 'AVAILABLE',
        (SELECT id FROM locations WHERE name='Detector Lab C3'), 'Calibrated 2024-01-15. Handle with anti-static precautions.'),
    ('DAQ Server Cluster 1', 'Data acquisition server cluster, 64 cores', 'COMPUTING', 'AVAILABLE',
        (SELECT id FROM locations WHERE name='Server Room B1'), 'Running DAQ firmware v4.2.1'),
    ('Pixel Detector Array B', 'High-granularity pixel detector array', 'DETECTOR', 'MAINTENANCE',
        (SELECT id FROM locations WHERE name='Detector Lab C3'), 'Under cooling system inspection'),
    ('Network Switch Core-01', '100GbE core network switch', 'NETWORKING', 'AVAILABLE',
        (SELECT id FROM locations WHERE name='Server Room B1'), 'Firmware updated 2024-03-01'),
    ('Oscilloscope Tek-500', 'Tektronix 5-series 8-channel oscilloscope', 'ELECTRONICS', 'AVAILABLE',
        (SELECT id FROM locations WHERE name='Electronics Workshop'), NULL),
    ('Liquid Nitrogen Dewar LN2-01', '500L liquid nitrogen storage dewar', 'CRYOGENICS', 'AVAILABLE',
        (SELECT id FROM locations WHERE name='Cryogenics Bay'), 'Last safety check: 2024-02-28'),
    ('Radiation Monitor RM-02', 'Area radiation monitoring unit', 'SAFETY', 'AVAILABLE',
        (SELECT id FROM locations WHERE name='Control Room Alpha'), 'Calibration due: 2024-06-01'),
    ('FPGA Test Board v3', 'Xilinx Ultrascale+ FPGA development/test board', 'ELECTRONICS', 'BOOKED',
        (SELECT id FROM locations WHERE name='Electronics Workshop'), NULL)
ON CONFLICT DO NOTHING;

-- Bookings
INSERT INTO bookings (resource_id, booked_by_id, start_time, end_time, purpose, status) VALUES
    (
        (SELECT id FROM resources WHERE name='Silicon Tracker Module A'),
        (SELECT id FROM users WHERE username='operator1'),
        NOW() + INTERVAL '1 day',
        NOW() + INTERVAL '1 day' + INTERVAL '4 hours',
        'Routine performance characterisation run',
        'APPROVED'
    ),
    (
        (SELECT id FROM resources WHERE name='FPGA Test Board v3'),
        (SELECT id FROM users WHERE username='operator2'),
        NOW(),
        NOW() + INTERVAL '8 hours',
        'Firmware validation test suite',
        'APPROVED'
    ),
    (
        (SELECT id FROM resources WHERE name='DAQ Server Cluster 1'),
        (SELECT id FROM users WHERE username='operator1'),
        NOW() + INTERVAL '3 days',
        NOW() + INTERVAL '3 days' + INTERVAL '12 hours',
        'Full detector readout stress test',
        'PENDING'
    )
ON CONFLICT DO NOTHING;

-- Maintenance Records
INSERT INTO maintenance_records (resource_id, reported_by_id, title, description, type, scheduled_start, scheduled_end, status) VALUES
    (
        (SELECT id FROM resources WHERE name='Pixel Detector Array B'),
        (SELECT id FROM users WHERE username='coordinator1'),
        'Cooling Loop Inspection',
        'Inspect and pressure-test cooling loop after anomalous temperature reading on 2024-03-10',
        'CORRECTIVE',
        NOW() - INTERVAL '1 day',
        NOW() + INTERVAL '2 days',
        'IN_PROGRESS'
    ),
    (
        (SELECT id FROM resources WHERE name='Liquid Nitrogen Dewar LN2-01'),
        (SELECT id FROM users WHERE username='coordinator1'),
        'Annual Safety Inspection',
        'Scheduled annual safety and pressure certification for LN2 storage vessel',
        'PREVENTIVE',
        NOW() + INTERVAL '7 days',
        NOW() + INTERVAL '7 days' + INTERVAL '3 hours',
        'PLANNED'
    )
ON CONFLICT DO NOTHING;

-- Daily Notes
INSERT INTO daily_notes (note_date, author_id, title, content, category, importance, pinned) VALUES
    (
        CURRENT_DATE,
        (SELECT id FROM users WHERE username='coordinator1'),
        'Morning Shift Handover',
        'All systems nominal at shift start. Pixel Detector Array B remains offline for cooling inspection – ETA completion is tomorrow EOD. DAQ Cluster 1 running nominal at 98% efficiency. Radiation levels in all zones within acceptable limits.',
        'OPERATIONAL',
        'NORMAL',
        TRUE
    ),
    (
        CURRENT_DATE,
        (SELECT id FROM users WHERE username='coordinator1'),
        'Network Maintenance Window Tonight',
        'Scheduled network switch firmware update window: 22:00 – 23:00 local time. Expect brief interruption to inter-building links. All operators should save work before 21:45.',
        'TECHNICAL',
        'HIGH',
        FALSE
    ),
    (
        CURRENT_DATE - INTERVAL '1 day',
        (SELECT id FROM users WHERE username='operator1'),
        'Silicon Tracker Calibration Complete',
        'Completed full strip-by-strip calibration of Silicon Tracker Module A. Results within 0.2% of reference values. Module cleared for next week scheduled test run.',
        'TECHNICAL',
        'NORMAL',
        FALSE
    )
ON CONFLICT DO NOTHING;
