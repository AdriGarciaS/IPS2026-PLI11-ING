-- Datos de ejemplo (dummy data).
-- Solo se cargan cuando la base de datos se crea desde cero, para no
-- duplicar filas si se mantiene entre ejecuciones.
-- Cada sentencia debe terminar en ";".



INSERT INTO personas (nombre, email) VALUES
    ('Ada Lovelace', 'ada@example.com'),
    ('Alan Turing', 'alan@example.com'),
    ('Peter Parker', 'peter@example.com'),
    ('Grace Hopper', 'grace@example.com');


INSERT INTO employees (first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES
    ('Lamine', 'Yamal', '12345678A', '2007-07-13', '+34600112233', 'SPORTS', 'Player', 1500000.0, 'MALE'),
    ('Pep', 'Guardiola', '87654321B', '1971-01-18', '+34611223344', 'SPORTS', 'Coach', 12000000.0, NULL),
    ('Sarah', 'Jenkins', '99887766C', '1985-04-12', '+34622334455', 'NON_SPORTS', 'General Manager', 95000.0, 'FEMALE');

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

-- Sample sports employees for the sports teams. The birth dates are relative
-- to the current year (age is counted by year), so every category always has
-- players of the right age.
INSERT INTO employees (first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES
    ('Marc', 'Serra', 'P0000001T', (strftime('%Y', 'now', 'localtime') - 24) || '-03-02', '+34650000001', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Pau', 'Ferrer', 'P0000002T', (strftime('%Y', 'now', 'localtime') - 23) || '-05-21', '+34650000002', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Dani', 'Rovira', 'P0000003T', (strftime('%Y', 'now', 'localtime') - 28) || '-07-09', '+34650000003', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Hector', 'Campos', 'P0000004T', (strftime('%Y', 'now', 'localtime') - 26) || '-09-30', '+34650000004', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Alex', 'Benet', 'P0000005T', (strftime('%Y', 'now', 'localtime') - 22) || '-11-12', '+34650000005', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Ruben', 'Arias', 'P0000006T', (strftime('%Y', 'now', 'localtime') - 27) || '-01-15', '+34650000006', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Julio', 'Costa', 'P0000007T', (strftime('%Y', 'now', 'localtime') - 27) || '-03-02', '+34650000007', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Iker', 'Lozano', 'P0000008T', (strftime('%Y', 'now', 'localtime') - 31) || '-05-21', '+34650000008', 'SPORTS', 'Player', 80000.0, 'MALE'),
    ('Alexia', 'Puig', 'P0000009T', (strftime('%Y', 'now', 'localtime') - 29) || '-07-09', '+34650000009', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Aitana', 'Mora', 'P0000010T', (strftime('%Y', 'now', 'localtime') - 26) || '-09-30', '+34650000010', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Patri', 'Guerra', 'P0000011T', (strftime('%Y', 'now', 'localtime') - 25) || '-11-12', '+34650000011', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Mapi', 'Leon', 'P0000012T', (strftime('%Y', 'now', 'localtime') - 30) || '-01-15', '+34650000012', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Irene', 'Pardo', 'P0000013T', (strftime('%Y', 'now', 'localtime') - 27) || '-03-02', '+34650000013', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Ona', 'Batlle', 'P0000014T', (strftime('%Y', 'now', 'localtime') - 24) || '-05-21', '+34650000014', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Cata', 'Coll', 'P0000015T', (strftime('%Y', 'now', 'localtime') - 23) || '-07-09', '+34650000015', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Salma', 'Prieto', 'P0000016T', (strftime('%Y', 'now', 'localtime') - 21) || '-09-30', '+34650000016', 'SPORTS', 'Player', 60000.0, 'FEMALE'),
    ('Pablo', 'Gavira', 'P0000017T', (strftime('%Y', 'now', 'localtime') - 17) || '-11-12', '+34650000017', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Fermin', 'Lopez', 'P0000018T', (strftime('%Y', 'now', 'localtime') - 18) || '-01-15', '+34650000018', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Marc', 'Bernal', 'P0000019T', (strftime('%Y', 'now', 'localtime') - 16) || '-03-02', '+34650000019', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Hugo', 'Duro', 'P0000020T', (strftime('%Y', 'now', 'localtime') - 17) || '-05-21', '+34650000020', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Unai', 'Hernandez', 'P0000021T', (strftime('%Y', 'now', 'localtime') - 18) || '-07-09', '+34650000021', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Toni', 'Fernandez', 'P0000022T', (strftime('%Y', 'now', 'localtime') - 16) || '-09-30', '+34650000022', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Gerard', 'Marti', 'P0000023T', (strftime('%Y', 'now', 'localtime') - 17) || '-11-12', '+34650000023', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Quim', 'Junyent', 'P0000024T', (strftime('%Y', 'now', 'localtime') - 18) || '-01-15', '+34650000024', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Dani', 'Rodriguez', 'P0000025T', (strftime('%Y', 'now', 'localtime') - 16) || '-03-02', '+34650000025', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Eric', 'Garcia', 'P0000026T', (strftime('%Y', 'now', 'localtime') - 17) || '-05-21', '+34650000026', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Hugo', 'Garcia', 'P0000027T', (strftime('%Y', 'now', 'localtime') - 14) || '-07-09', '+34650000027', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Leo', 'Garrido', 'P0000028T', (strftime('%Y', 'now', 'localtime') - 15) || '-09-30', '+34650000028', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Pablo', 'Ruiz', 'P0000029T', (strftime('%Y', 'now', 'localtime') - 14) || '-11-12', '+34650000029', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Mario', 'Sanz', 'P0000030T', (strftime('%Y', 'now', 'localtime') - 15) || '-01-15', '+34650000030', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Alvaro', 'Gil', 'P0000031T', (strftime('%Y', 'now', 'localtime') - 14) || '-03-02', '+34650000031', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Adrian', 'Ortega', 'P0000032T', (strftime('%Y', 'now', 'localtime') - 15) || '-05-21', '+34650000032', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Diego', 'Molina', 'P0000033T', (strftime('%Y', 'now', 'localtime') - 14) || '-07-09', '+34650000033', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Ivan', 'Romero', 'P0000034T', (strftime('%Y', 'now', 'localtime') - 15) || '-09-30', '+34650000034', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Sergio', 'Nieto', 'P0000035T', (strftime('%Y', 'now', 'localtime') - 14) || '-11-12', '+34650000035', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Lucia', 'Vidal', 'P0000036T', (strftime('%Y', 'now', 'localtime') - 14) || '-01-15', '+34650000036', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Carla', 'Pastor', 'P0000037T', (strftime('%Y', 'now', 'localtime') - 15) || '-03-02', '+34650000037', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Noa', 'Herrero', 'P0000038T', (strftime('%Y', 'now', 'localtime') - 14) || '-05-21', '+34650000038', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Martin', 'Cano', 'P0000039T', (strftime('%Y', 'now', 'localtime') - 12) || '-07-09', '+34650000039', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Lucas', 'Prats', 'P0000040T', (strftime('%Y', 'now', 'localtime') - 13) || '-09-30', '+34650000040', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Daniel', 'Soto', 'P0000041T', (strftime('%Y', 'now', 'localtime') - 12) || '-11-12', '+34650000041', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Marcos', 'Rey', 'P0000042T', (strftime('%Y', 'now', 'localtime') - 13) || '-01-15', '+34650000042', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Sofia', 'Calvo', 'P0000043T', (strftime('%Y', 'now', 'localtime') - 12) || '-03-02', '+34650000043', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Julia', 'Fuentes', 'P0000044T', (strftime('%Y', 'now', 'localtime') - 13) || '-05-21', '+34650000044', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Paula', 'Mendez', 'P0000045T', (strftime('%Y', 'now', 'localtime') - 12) || '-07-09', '+34650000045', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Vera', 'Iglesias', 'P0000046T', (strftime('%Y', 'now', 'localtime') - 13) || '-09-30', '+34650000046', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Nico', 'Vidal', 'P0000047T', (strftime('%Y', 'now', 'localtime') - 13) || '-11-12', '+34650000047', 'SPORTS', 'Player', 0.0, NULL),
    ('Bruno', 'Santos', 'P0000048T', (strftime('%Y', 'now', 'localtime') - 10) || '-01-15', '+34650000048', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Gael', 'Ramos', 'P0000049T', (strftime('%Y', 'now', 'localtime') - 11) || '-03-02', '+34650000049', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Liam', 'Castro', 'P0000050T', (strftime('%Y', 'now', 'localtime') - 10) || '-05-21', '+34650000050', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Thiago', 'Marin', 'P0000051T', (strftime('%Y', 'now', 'localtime') - 11) || '-07-09', '+34650000051', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Emma', 'Ortiz', 'P0000052T', (strftime('%Y', 'now', 'localtime') - 10) || '-09-30', '+34650000052', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Olivia', 'Rubio', 'P0000053T', (strftime('%Y', 'now', 'localtime') - 11) || '-11-12', '+34650000053', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Valeria', 'Moreno', 'P0000054T', (strftime('%Y', 'now', 'localtime') - 10) || '-01-15', '+34650000054', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Enzo', 'Vargas', 'P0000055T', (strftime('%Y', 'now', 'localtime') - 8) || '-03-02', '+34650000055', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Mateo', 'Herrera', 'P0000056T', (strftime('%Y', 'now', 'localtime') - 9) || '-05-21', '+34650000056', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Izan', 'Medina', 'P0000057T', (strftime('%Y', 'now', 'localtime') - 8) || '-07-09', '+34650000057', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Oliver', 'Aguilar', 'P0000058T', (strftime('%Y', 'now', 'localtime') - 9) || '-09-30', '+34650000058', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Alba', 'Delgado', 'P0000059T', (strftime('%Y', 'now', 'localtime') - 8) || '-11-12', '+34650000059', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Chloe', 'Pena', 'P0000060T', (strftime('%Y', 'now', 'localtime') - 9) || '-01-15', '+34650000060', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Lola', 'Cabrera', 'P0000061T', (strftime('%Y', 'now', 'localtime') - 8) || '-03-02', '+34650000061', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Dylan', 'Reyes', 'P0000062T', (strftime('%Y', 'now', 'localtime') - 6) || '-05-21', '+34650000062', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Eric', 'Flores', 'P0000063T', (strftime('%Y', 'now', 'localtime') - 7) || '-07-09', '+34650000063', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Biel', 'Leal', 'P0000064T', (strftime('%Y', 'now', 'localtime') - 6) || '-09-30', '+34650000064', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Pol', 'Vicente', 'P0000065T', (strftime('%Y', 'now', 'localtime') - 7) || '-11-12', '+34650000065', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Ariadna', 'Pascual', 'P0000066T', (strftime('%Y', 'now', 'localtime') - 6) || '-01-15', '+34650000066', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Mia', 'Crespo', 'P0000067T', (strftime('%Y', 'now', 'localtime') - 7) || '-03-02', '+34650000067', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Gala', 'Rojas', 'P0000068T', (strftime('%Y', 'now', 'localtime') - 6) || '-05-21', '+34650000068', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Luis', 'Moreno', 'C0000001T', '1978-04-11', '+34660000001', 'SPORTS', 'Coach', 45000.0, NULL),
    ('Ana', 'Torres', 'C0000002T', '1981-04-12', '+34660000002', 'SPORTS', 'Coach', 45000.0, NULL),
    ('Carlos', 'Vega', 'C0000003T', '1984-04-13', '+34660000003', 'SPORTS', 'Coach', 45000.0, NULL),
    ('Marta', 'Gil', 'C0000004T', '1987-04-14', '+34660000004', 'SPORTS', 'Coach', 45000.0, NULL),
    ('Javier', 'Ruiz', 'C0000005T', '1990-04-15', '+34660000005', 'SPORTS', 'Coach', 45000.0, NULL),
    ('Elena', 'Navarro', 'C0000006T', '1993-04-16', '+34660000006', 'SPORTS', 'Coach', 45000.0, NULL);

-- A sample team, so players that already belong to a team can be seen.
INSERT INTO TEAM (name, category, gender, first_coach_id, second_coach_id, created_at) VALUES
    ('Juvenil A', 'JUVENIL', 'MALE',
     (SELECT id FROM employees WHERE national_id = 'C0000001T'),
     (SELECT id FROM employees WHERE national_id = 'C0000003T'),
     datetime('now', 'localtime'));

INSERT INTO TEAM_MEMBER (team_id, employee_id, role, task)
SELECT (SELECT id FROM TEAM WHERE name = 'Juvenil A'), id, 'PLAYER', NULL FROM employees
WHERE national_id IN ('P0000017T', 'P0000018T', 'P0000019T', 'P0000020T', 'P0000021T', 'P0000022T', 'P0000023T');

INSERT INTO TEAM_MEMBER (team_id, employee_id, role, task) VALUES
    ((SELECT id FROM TEAM WHERE name = 'Juvenil A'), (SELECT id FROM employees WHERE national_id = 'C0000002T'), 'STAFF', 'Goalkeepers');
