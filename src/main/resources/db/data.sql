-- Datos de ejemplo (dummy data).
-- Solo se cargan cuando la base de datos se crea desde cero, para no
-- duplicar filas si se mantiene entre ejecuciones.
-- Cada sentencia debe terminar en ";".



INSERT INTO personas (nombre, email) VALUES
    ('Ada Lovelace', 'ada@example.com'),
    ('Alan Turing', 'alan@example.com'),
    ('Peter Parker', 'peter@example.com'),
    ('Grace Hopper', 'grace@example.com');


INSERT INTO employees (first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary) VALUES
    ('Lamine', 'Yamal', '12345678A', '2007-07-13', '+34600112233', 'SPORTS', 'Player', 1500000.0),
    ('Pep', 'Guardiola', '87654321B', '1971-01-18', '+34611223344', 'SPORTS', 'Coach', 12000000.0),
    ('Sarah', 'Jenkins', '99887766C', '1985-04-12', '+34622334455', 'NON_SPORTS', 'General Manager', 95000.0);

INSERT INTO MERCHANDISING (name, type, available_units, price) VALUES
    ('Home Shirt 26/27', 'Clothing', 25, 59.99),
    ('Away Shirt 26/27', 'Clothing', 20, 59.99),
    ('Training Shirt', 'Clothing', 40, 34.95),
    ('Club Hoodie', 'Clothing', 15, 49.90),
    ('Club Scarf', 'Accessories', 50, 14.95),
    ('Club Cap', 'Accessories', 30, 19.95),
    ('Club Keyring', 'Souvenirs', 100, 4.50),
    ('Club Mug', 'Souvenirs', 60, 9.95),
    ('Signed Team Poster', 'Souvenirs', 5, 24.90),
    ('Official Match Ball', 'Equipment', 12, 29.95),
    ('Goalkeeper Gloves', 'Equipment', 8, 39.95);

INSERT INTO FACILITY (id, name) VALUES
    (1, 'Main Pitch'),
    (2, 'Training Pitch 1'),
    (3, 'Training Pitch 2'),
    (4, 'Gym'),
    (5, 'Indoor Court');

-- Sample team uses for the next 32 days, relative to the day in which the
-- database is created (so there is always data to see).
WITH RECURSIVE days(n) AS (SELECT 0 UNION ALL SELECT n + 1 FROM days WHERE n < 31)
INSERT INTO FACILITY_TEAM_USE (facility_id, team_name, use_date, start_time, end_time)
SELECT 2, 'First Team', date('now', 'localtime', '+' || n || ' days'), '10:00', '12:00' FROM days
UNION ALL
SELECT 2, 'U19 Team', date('now', 'localtime', '+' || n || ' days'), '18:00', '20:00' FROM days
UNION ALL
SELECT 3, 'U16 Team', date('now', 'localtime', '+' || n || ' days'), '17:00', '19:00' FROM days
UNION ALL
SELECT 4, 'First Team', date('now', 'localtime', '+' || n || ' days'), '09:00', '10:30' FROM days
UNION ALL
SELECT 4, 'Women''s Team', date('now', 'localtime', '+' || n || ' days'), '16:00', '17:30' FROM days
UNION ALL
SELECT 1, 'First Team (match)', date('now', 'localtime', '+' || n || ' days'), '17:00', '19:00' FROM days WHERE n % 7 = 2;

-- An existing reservation, so the "Reserved" periods can be seen.
INSERT INTO FACILITY_RESERVATION (facility_id, holder_name, card_number, reservation_date, start_time, end_time, hours, total_price, created_at) VALUES
    (2, 'Laura Perez', '**** **** **** 1111', date('now', 'localtime', '+1 day'), '14:00', '16:00', 2, 100.0, datetime('now', 'localtime'));
    
insert into work_schedule (id, employee_id, week_day, start_time, end_time) VALUES
	(0, 1, 2, '12:30', '14:30');

