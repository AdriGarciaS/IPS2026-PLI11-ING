-- Datos de ejemplo (dummy data).
-- Solo se cargan cuando la base de datos se crea desde cero.
-- Cada sentencia debe terminar en ";".

INSERT INTO employees (first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender, username, password) VALUES
    ('Lamine', 'Yamal', '12345678A', '2007-07-13', '+34600112233', 'SPORTS', 'Player', 1500000.0, 'MALE', 'lamine_yamal', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    ('Joan', 'García', '65437890W', '2002-04-22', '+34600412444', 'SPORTS', 'Player', 1100000.0, 'MALE', 'joan_garcia', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    ('Pedri', 'González', '12345678P', '2002-11-25', '+34600111222', 'SPORTS', 'Player', 900000.0, 'MALE', 'pedri_gonzalez', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    ('Pep', 'Guardiola', '87654321B', '1971-01-18', '+34611223344', 'SPORTS', 'Coach', 12000000.0, 'MALE', 'pep_guardiola', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    
    ('Sarah', 'Jenkins', '99887766C', '1985-04-12', '+34622334455', 'NON_SPORTS', 'General Manager', 95000.0, 'FEMALE', 'sarah_jenkins', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    ('Jackie', 'Taylor', '62671892R', '1978-11-09', '+34669571133', 'NON_SPORTS', 'Facility Manager', 70000.0, 'FEMALE', 'jackie_taylor', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    
    ('Carlos', 'Martínez', '74392871L', '2001-12-19', '+34626014732', 'NON_SPORTS', 'Gardener', 30000.0, 'MALE', 'carlos_martinez', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    ('Rosa', 'Vilas', '87290851K', '1988-02-02', '+34692449181', 'NON_SPORTS', 'Gardener', 30000.0, 'FEMALE', 'rosa_vilas', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    ('Clara', 'Díez', '92378567M', '2002-08-21', '+34611311952', 'NON_SPORTS', 'Gardener', 30000.0, 'FEMALE', 'clara_diez', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
    
    ('Jaime', 'Gómez', '12345678J', '1990-05-15', '+34600112982', 'NON_SPORTS', 'Doctor', 80000.0, 'MALE', 'jaime_gomez', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
	('Laura', 'Martínez', '87654321L', '1985-09-20', '+34600412444', 'NON_SPORTS', 'Doctor', 80000.0, 'FEMALE', 'laura_martinez', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
	
	('Celia', 'Lobo', '75829384C', '2003-03-10', '+34601441957', 'NON_SPORTS', 'Store Employee', 30000.0, 'FEMALE', 'celia_lobo', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
	('Llara', 'Molla', '17223479K', '2005-12-18', '+34626665900', 'NON_SPORTS', 'Ticket Seller', 30000.0, 'FEMALE', 'llara_molla', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO'),
	
	('Jordi', 'Soler', '19283746J', '1995-07-22', '+34600112233', 'NON_SPORTS', 'Press Officer', 30000.0, 'MALE', 'jordi_soler', '$2a$12$GsZH5SrT4iF0BCW8fCpj4OJ6CWrj6UknlMVcNtDR0yvYoUu6AirZO');

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

INSERT INTO FACILITY_RESERVATION (facility_id, holder_name, card_number, reservation_date, start_time, end_time, hours, total_price, created_at) VALUES
    (2, 'Laura Perez', '**** **** **** 1111', date('now', 'localtime', '+1 day'), '14:00', '16:00', 2, 100.0, datetime('now', 'localtime'));
    
INSERT INTO work_schedule (employee_id, week_day, start_time, end_time) VALUES
	(7, 1, '09:00', '17:00'),
	(7, 2, '09:00', '17:00'),
	(7, 3, '09:00', '17:00'),
	(8, 2, '09:00', '17:00'),
	(8, 3, '09:00', '17:00'),
	(8, 6, '09:00', '16:00');

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
    ('Nico', 'Vidal', 'P0000047T', (strftime('%Y', 'now', 'localtime') - 13) || '-11-12', '+34650000047', 'SPORTS', 'Player', 0.0, 'MALE'),
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
    ('Luis', 'Moreno', 'C0000001T', '1978-04-11', '+34660000001', 'SPORTS', 'Coach', 45000.0, 'MALE'),
    ('Ana', 'Torres', 'C0000002T', '1981-04-12', '+34660000002', 'SPORTS', 'Coach', 45000.0, 'FEMALE'),
    ('Carlos', 'Vega', 'C0000003T', '1984-04-13', '+34660000003', 'SPORTS', 'Coach', 45000.0, 'MALE'),
    ('Marta', 'Gil', 'C0000004T', '1987-04-14', '+34660000004', 'SPORTS', 'Coach', 45000.0, 'FEMALE'),
    ('Javier', 'Ruiz', 'C0000005T', '1990-04-15', '+34660000005', 'SPORTS', 'Coach', 45000.0, 'MALE'),
    ('Elena', 'Navarro', 'C0000006T', '1993-04-16', '+34660000006', 'SPORTS', 'Coach', 45000.0, 'FEMALE');

INSERT INTO employees (first_name, last_name, national_id, birth_date, phone_number, category, position, gross_annual_salary, gender) VALUES
    ('Raul', 'Peña', 'P0000069T', (strftime('%Y', 'now', 'localtime') - 25) || '-08-10', '+34650000069', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Oscar', 'Mingueza', 'P0000070T', (strftime('%Y', 'now', 'localtime') - 27) || '-10-01', '+34650000070', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Nil', 'Prat', 'P0000071T', (strftime('%Y', 'now', 'localtime') - 21) || '-12-05', '+34650000071', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Jan', 'Virgili', 'P0000072T', (strftime('%Y', 'now', 'localtime') - 20) || '-02-14', '+34650000072', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Biel', 'Ferrer', 'P0000073T', (strftime('%Y', 'now', 'localtime') - 22) || '-04-03', '+34650000073', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Joel', 'Rubio', 'P0000074T', (strftime('%Y', 'now', 'localtime') - 24) || '-06-22', '+34650000074', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Aleix', 'Garrido', 'P0000075T', (strftime('%Y', 'now', 'localtime') - 26) || '-08-10', '+34650000075', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Mikel', 'Iturbe', 'P0000076T', (strftime('%Y', 'now', 'localtime') - 29) || '-10-01', '+34650000076', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Sergi', 'Domenech', 'P0000077T', (strftime('%Y', 'now', 'localtime') - 23) || '-12-05', '+34650000077', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Victor', 'Barrios', 'P0000078T', (strftime('%Y', 'now', 'localtime') - 28) || '-02-14', '+34650000078', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Adria', 'Sala', 'P0000079T', (strftime('%Y', 'now', 'localtime') - 21) || '-04-03', '+34650000079', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Gabri', 'Coll', 'P0000080T', (strftime('%Y', 'now', 'localtime') - 30) || '-06-22', '+34650000080', 'SPORTS', 'Player', 70000.0, 'MALE'),
    ('Claudia', 'Pina', 'P0000081T', (strftime('%Y', 'now', 'localtime') - 24) || '-08-10', '+34650000081', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Laia', 'Codina', 'P0000082T', (strftime('%Y', 'now', 'localtime') - 25) || '-10-01', '+34650000082', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Ingrid', 'Engen', 'P0000083T', (strftime('%Y', 'now', 'localtime') - 29) || '-12-05', '+34650000083', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Sandra', 'Paños', 'P0000084T', (strftime('%Y', 'now', 'localtime') - 32) || '-02-14', '+34650000084', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Bruna', 'Vilamala', 'P0000085T', (strftime('%Y', 'now', 'localtime') - 23) || '-04-03', '+34650000085', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Marta', 'Torrejon', 'P0000086T', (strftime('%Y', 'now', 'localtime') - 34) || '-06-22', '+34650000086', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Lucia', 'Garcia', 'P0000087T', (strftime('%Y', 'now', 'localtime') - 27) || '-08-10', '+34650000087', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Jana', 'Fernandez', 'P0000088T', (strftime('%Y', 'now', 'localtime') - 22) || '-10-01', '+34650000088', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Nuria', 'Rabano', 'P0000089T', (strftime('%Y', 'now', 'localtime') - 26) || '-12-05', '+34650000089', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Ana', 'Crnogorcevic', 'P0000090T', (strftime('%Y', 'now', 'localtime') - 33) || '-02-14', '+34650000090', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Gemma', 'Font', 'P0000091T', (strftime('%Y', 'now', 'localtime') - 25) || '-04-03', '+34650000091', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Esther', 'Gonzalez', 'P0000092T', (strftime('%Y', 'now', 'localtime') - 31) || '-06-22', '+34650000092', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Berta', 'Pujadas', 'P0000093T', (strftime('%Y', 'now', 'localtime') - 20) || '-08-10', '+34650000093', 'SPORTS', 'Player', 55000.0, 'FEMALE'),
    ('Ivan', 'Cuesta', 'P0000094T', (strftime('%Y', 'now', 'localtime') - 16) || '-10-01', '+34650000094', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Pau', 'Prim', 'P0000095T', (strftime('%Y', 'now', 'localtime') - 17) || '-12-05', '+34650000095', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Arnau', 'Pradas', 'P0000096T', (strftime('%Y', 'now', 'localtime') - 18) || '-02-14', '+34650000096', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Roger', 'Ortiz', 'P0000097T', (strftime('%Y', 'now', 'localtime') - 16) || '-04-03', '+34650000097', 'SPORTS', 'Player', 6000.0, 'MALE'),
    ('Ariana', 'Arias', 'P0000098T', (strftime('%Y', 'now', 'localtime') - 16) || '-06-22', '+34650000098', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Carla', 'Julia', 'P0000099T', (strftime('%Y', 'now', 'localtime') - 17) || '-08-10', '+34650000099', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Martina', 'Fernandez', 'P0000100T', (strftime('%Y', 'now', 'localtime') - 18) || '-10-01', '+34650000100', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Noelia', 'Ramos', 'P0000101T', (strftime('%Y', 'now', 'localtime') - 16) || '-12-05', '+34650000101', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Judit', 'Pujols', 'P0000102T', (strftime('%Y', 'now', 'localtime') - 17) || '-02-14', '+34650000102', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Aina', 'Duran', 'P0000103T', (strftime('%Y', 'now', 'localtime') - 18) || '-04-03', '+34650000103', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Erika', 'Serra', 'P0000104T', (strftime('%Y', 'now', 'localtime') - 16) || '-06-22', '+34650000104', 'SPORTS', 'Player', 4000.0, 'FEMALE'),
    ('Elsa', 'Marques', 'P0000105T', (strftime('%Y', 'now', 'localtime') - 14) || '-08-10', '+34650000105', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Iria', 'Lago', 'P0000106T', (strftime('%Y', 'now', 'localtime') - 15) || '-10-01', '+34650000106', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Mar', 'Roca', 'P0000107T', (strftime('%Y', 'now', 'localtime') - 14) || '-12-05', '+34650000107', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Abril', 'Sanchez', 'P0000108T', (strftime('%Y', 'now', 'localtime') - 15) || '-02-14', '+34650000108', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Ian', 'Bravo', 'P0000109T', (strftime('%Y', 'now', 'localtime') - 12) || '-04-03', '+34650000109', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Hector', 'Lara', 'P0000110T', (strftime('%Y', 'now', 'localtime') - 13) || '-06-22', '+34650000110', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Nora', 'Giner', 'P0000111T', (strftime('%Y', 'now', 'localtime') - 12) || '-08-10', '+34650000111', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Ines', 'Bosch', 'P0000112T', (strftime('%Y', 'now', 'localtime') - 13) || '-10-01', '+34650000112', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Clara', 'Mas', 'P0000113T', (strftime('%Y', 'now', 'localtime') - 12) || '-12-05', '+34650000113', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Teo', 'Lorente', 'P0000114T', (strftime('%Y', 'now', 'localtime') - 10) || '-02-14', '+34650000114', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Aaron', 'Caballero', 'P0000115T', (strftime('%Y', 'now', 'localtime') - 11) || '-04-03', '+34650000115', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Unai', 'Esteve', 'P0000116T', (strftime('%Y', 'now', 'localtime') - 10) || '-06-22', '+34650000116', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Daniela', 'Sole', 'P0000117T', (strftime('%Y', 'now', 'localtime') - 11) || '-08-10', '+34650000117', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Aitana', 'Ribas', 'P0000118T', (strftime('%Y', 'now', 'localtime') - 10) || '-10-01', '+34650000118', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Africa', 'Pons', 'P0000119T', (strftime('%Y', 'now', 'localtime') - 11) || '-12-05', '+34650000119', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Elena', 'Vives', 'P0000120T', (strftime('%Y', 'now', 'localtime') - 10) || '-02-14', '+34650000120', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Marc', 'Planas', 'P0000121T', (strftime('%Y', 'now', 'localtime') - 8) || '-04-03', '+34650000121', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Hugo', 'Riera', 'P0000122T', (strftime('%Y', 'now', 'localtime') - 9) || '-06-22', '+34650000122', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Leo', 'Font', 'P0000123T', (strftime('%Y', 'now', 'localtime') - 8) || '-08-10', '+34650000123', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Carlota', 'Grau', 'P0000124T', (strftime('%Y', 'now', 'localtime') - 9) || '-10-01', '+34650000124', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Laia', 'Bonet', 'P0000125T', (strftime('%Y', 'now', 'localtime') - 8) || '-12-05', '+34650000125', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Ona', 'Mir', 'P0000126T', (strftime('%Y', 'now', 'localtime') - 9) || '-02-14', '+34650000126', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Julia', 'Soler', 'P0000127T', (strftime('%Y', 'now', 'localtime') - 8) || '-04-03', '+34650000127', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Nil', 'Casas', 'P0000128T', (strftime('%Y', 'now', 'localtime') - 6) || '-06-22', '+34650000128', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Max', 'Roig', 'P0000129T', (strftime('%Y', 'now', 'localtime') - 7) || '-08-10', '+34650000129', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Gal', 'Serra', 'P0000130T', (strftime('%Y', 'now', 'localtime') - 6) || '-10-01', '+34650000130', 'SPORTS', 'Player', 0.0, 'MALE'),
    ('Lia', 'Vidal', 'P0000131T', (strftime('%Y', 'now', 'localtime') - 7) || '-12-05', '+34650000131', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Arlet', 'Puig', 'P0000132T', (strftime('%Y', 'now', 'localtime') - 6) || '-02-14', '+34650000132', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Duna', 'Costa', 'P0000133T', (strftime('%Y', 'now', 'localtime') - 7) || '-04-03', '+34650000133', 'SPORTS', 'Player', 0.0, 'FEMALE'),
    ('Maria', 'Tort', 'P0000134T', (strftime('%Y', 'now', 'localtime') - 6) || '-06-22', '+34650000134', 'SPORTS', 'Player', 0.0, 'FEMALE');

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

INSERT INTO TEAM (name, category, gender, first_coach_id, second_coach_id, created_at) VALUES
    ('First Team', 'FIRST_TEAM', 'MALE',
     (SELECT id FROM employees WHERE national_id = '87654321B'),
     (SELECT id FROM employees WHERE national_id = 'C0000005T'),
     datetime('now', 'localtime')),
    ('Women''s First Team', 'FIRST_TEAM', 'FEMALE',
     (SELECT id FROM employees WHERE national_id = 'C0000004T'),
     (SELECT id FROM employees WHERE national_id = 'C0000006T'),
     datetime('now', 'localtime'));

INSERT INTO TEAM_MEMBER (team_id, employee_id, role, task)
SELECT (SELECT id FROM TEAM WHERE name = 'First Team'), id, 'PLAYER', NULL FROM employees
WHERE national_id IN ('12345678A', '12345678P', 'P0000001T', 'P0000002T', 'P0000003T', 'P0000004T', 'P0000005T');

INSERT INTO TEAM_MEMBER (team_id, employee_id, role, task)
SELECT (SELECT id FROM TEAM WHERE name = 'Women''s First Team'), id, 'PLAYER', NULL FROM employees
WHERE national_id IN ('P0000009T', 'P0000010T', 'P0000011T', 'P0000012T', 'P0000013T', 'P0000014T', 'P0000015T');

WITH RECURSIVE days(n) AS (SELECT 0 UNION ALL SELECT n + 1 FROM days WHERE n < 31)
INSERT INTO trainings (team_id, coach_id, training_date, start_time, end_time, facility_id)
SELECT t.id, t.first_coach_id, date('now', 'localtime', '+' || n || ' days'), '10:00', '12:00', 2 
FROM days, TEAM t WHERE t.name = 'First Team'

UNION ALL

SELECT t.id, t.first_coach_id, date('now', 'localtime', '+' || n || ' days'), '09:00', '10:30', 4 
FROM days, TEAM t WHERE t.name = 'First Team' AND n % 2 = 1

UNION ALL

SELECT t.id, t.first_coach_id, date('now', 'localtime', '+' || n || ' days'), '18:00', '20:00', 2 
FROM days, TEAM t WHERE t.name = 'Juvenil A'

UNION ALL

SELECT t.id, t.first_coach_id, date('now', 'localtime', '+' || n || ' days'), '17:00', '19:00', 3 
FROM days, TEAM t WHERE t.name = 'Women''s First Team'

UNION ALL

SELECT t.id, t.first_coach_id, date('now', 'localtime', '+' || n || ' days'), '16:00', '17:30', 4 
FROM days, TEAM t WHERE t.name = 'Women''s First Team' AND n % 2 = 0;

WITH RECURSIVE days(n) AS (SELECT 0 UNION ALL SELECT n + 1 FROM days WHERE n < 31)
INSERT INTO matches (team_id, match_date, start_time, end_time, rival, is_home, facility_id)
SELECT (SELECT id FROM TEAM WHERE name = 'First Team'), date('now', 'localtime', '+' || n || ' days'), '17:00', '19:00',
       CASE (n / 7) % 3 WHEN 0 THEN 'Real Madrid' WHEN 1 THEN 'Sevilla FC' ELSE 'Valencia CF' END, 1, 1
FROM days WHERE n % 7 = 2

UNION ALL

SELECT (SELECT id FROM TEAM WHERE name = 'First Team'), date('now', 'localtime', '+' || n || ' days'), '18:00', '20:00',
       CASE (n / 7) % 3 WHEN 0 THEN 'Atletico Madrid' WHEN 1 THEN 'Athletic Club' ELSE 'Real Betis' END, 0, NULL
FROM days WHERE n % 7 = 5

UNION ALL

SELECT (SELECT id FROM TEAM WHERE name = 'Women''s First Team'), date('now', 'localtime', '+' || n || ' days'), '12:00', '14:00',
       CASE (n / 7) % 2 WHEN 0 THEN 'Real Madrid Femenino' ELSE 'Levante UD' END, 1, 1
FROM days WHERE n % 7 = 6

UNION ALL

SELECT (SELECT id FROM TEAM WHERE name = 'Juvenil A'), date('now', 'localtime', '+' || n || ' days'), '11:00', '13:00',
       'RCD Espanyol Juvenil', 0, NULL
FROM days WHERE n % 7 = 3;

INSERT INTO trainings (team_id, coach_id, training_date, start_time, end_time, facility_id)
VALUES (
    (SELECT id FROM TEAM WHERE name = 'First Team'),
    (SELECT first_coach_id FROM TEAM WHERE name = 'First Team'),
    '2026-10-15', '09:00', '11:00', 5
);

INSERT INTO SHAREHOLDER (first_name, last_name, national_id) VALUES
    ('Laura', 'Pérez Gil', '11111111H'),
    ('Marcos', 'Ruiz Soto', '22222222J'),
    ('Irene', 'Castillo Mora', '33333333P'),
    ('Javier', 'Ortega Lima', '44444444A');

INSERT INTO SHARE_CAMPAIGN (name, nominal_price, total_shares, status) VALUES
    ('Founding shares 2023', 9.50, 1000, 'FINISHED'),
    ('Capital increase 2024', 10.00, 1000, 'FINISHED'),
    ('Capital increase 2026', 14.00, 1000, 'FINISHED');

INSERT INTO SHARE_HOLDING (shareholder_id, campaign_id, purchase_date, purchase_price) VALUES
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '11111111H'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Founding shares 2023'), '2023-02-10 10:15:00', 9.50),
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '11111111H'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Capital increase 2024'), '2024-03-12 12:40:00', 10.00),
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '22222222J'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Capital increase 2024'), '2024-04-02 09:05:00', 15.00),
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '22222222J'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Capital increase 2026'), datetime('now', 'localtime', 'start of day', '-8 days', '+12 hours', '+30 minutes'), 14.20),
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '33333333P'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Founding shares 2023'), '2023-03-01 17:30:00', 9.80),
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '33333333P'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Capital increase 2026'), datetime('now', 'localtime', 'start of day', '-20 days', '+16 hours', '+10 minutes'), 14.80);

WITH RECURSIVE numbers(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM numbers WHERE n < 3000),
ranges(national_id, campaign, first_id, last_id) AS (
    SELECT '11111111H', 'Founding shares 2023', 1, 300
    UNION ALL SELECT '11111111H', 'Founding shares 2023', 601, 800
    UNION ALL SELECT '33333333P', 'Founding shares 2023', 301, 500
    UNION ALL SELECT '11111111H', 'Capital increase 2024', 1001, 1300
    UNION ALL SELECT '22222222J', 'Capital increase 2024', 1301, 1650
    UNION ALL SELECT '22222222J', 'Capital increase 2024', 1701, 1750
    UNION ALL SELECT '22222222J', 'Capital increase 2026', 2001, 2250
    UNION ALL SELECT '33333333P', 'Capital increase 2026', 2251, 2500
    UNION ALL SELECT '33333333P', 'Capital increase 2026', 2601, 2700
)
INSERT INTO SHARE (id, holding_id)
SELECT n.n, h.id
FROM ranges r
JOIN SHAREHOLDER s ON s.national_id = r.national_id
JOIN SHARE_CAMPAIGN c ON c.name = r.campaign
JOIN SHARE_HOLDING h ON h.shareholder_id = s.id AND h.campaign_id = c.id
JOIN numbers n ON n.n BETWEEN r.first_id AND r.last_id;

INSERT INTO MARKET_PRICE (price_date, price) VALUES
    (date('now', 'localtime', '-3 days'), 12.10),
    (date('now', 'localtime', '-2 days'), 12.25),
    (date('now', 'localtime', '-1 days'), 12.30);

INSERT INTO MERCHANDISING_SALE (sale_number, merch_id, units_sold, total_price, sale_date) VALUES
    (1, (SELECT id FROM MERCHANDISING WHERE name = 'Home Shirt 26/27'), 2, 119.98, datetime('now', 'localtime', 'start of day', '-1 days', '+18 hours', '+20 minutes')),
    (1, (SELECT id FROM MERCHANDISING WHERE name = 'Club Scarf'), 1, 14.95, datetime('now', 'localtime', 'start of day', '-1 days', '+18 hours', '+20 minutes')),
    (1, (SELECT id FROM MERCHANDISING WHERE name = 'Club Mug'), 3, 29.85, datetime('now', 'localtime', 'start of day', '-1 days', '+18 hours', '+20 minutes')),
    (2, (SELECT id FROM MERCHANDISING WHERE name = 'Official Match Ball'), 1, 29.95, datetime('now', 'localtime', 'start of day', '-4 days', '+11 hours', '+5 minutes')),
    (3, (SELECT id FROM MERCHANDISING WHERE name = 'Club Hoodie'), 1, 49.90, datetime('now', 'localtime', 'start of day', '-9 days', '+17 hours', '+40 minutes')),
    (3, (SELECT id FROM MERCHANDISING WHERE name = 'Club Cap'), 2, 39.90, datetime('now', 'localtime', 'start of day', '-9 days', '+17 hours', '+40 minutes')),
    (4, (SELECT id FROM MERCHANDISING WHERE name = 'Club Keyring'), 4, 18.00, datetime('now', 'localtime', 'start of day', '-45 days', '+12 hours'));

INSERT INTO MATCH_TICKET (purchase_number, match_id, grandstand, section, row_num, seat_num, price, created_at) VALUES
    (1, 1, 'A', 'B', 4, 7, 30.0, datetime('now', 'localtime', 'start of day', '-2 days', '+10 hours', '+15 minutes')),
    (1, 1, 'A', 'B', 4, 8, 30.0, datetime('now', 'localtime', 'start of day', '-2 days', '+10 hours', '+15 minutes')),
    (1, 1, 'A', 'B', 4, 9, 30.0, datetime('now', 'localtime', 'start of day', '-2 days', '+10 hours', '+15 minutes')),
    (2, 1, 'C', 'A', 1, 3, 30.0, datetime('now', 'localtime', 'start of day', '-6 days', '+20 hours', '+30 minutes')),
    (3, 1, 'D', 'F', 10, 1, 30.0, datetime('now', 'localtime', 'start of day', '-12 days', '+9 hours', '+50 minutes')),
    (3, 1, 'D', 'F', 10, 2, 30.0, datetime('now', 'localtime', 'start of day', '-12 days', '+9 hours', '+50 minutes'));

INSERT INTO SEASON_TICKET (season, holder_name, price, created_at) VALUES
    ('2026/27', 'Carlos Medina', 350.00, datetime('now', 'localtime', 'start of day', '-3 days', '+9 hours', '+45 minutes')),
    ('2026/27', 'Lucia Romero', 350.00, datetime('now', 'localtime', 'start of day', '-15 days', '+13 hours', '+20 minutes')),
    ('2026/27', 'Pablo Ortiz', 280.00, datetime('now', 'localtime', 'start of day', '-40 days', '+19 hours'));

INSERT INTO FACILITY_RESERVATION (facility_id, holder_name, card_number, reservation_date, start_time, end_time, hours, total_price, created_at) VALUES
    (4, 'Mario Blanco', '**** **** **** 2222', date('now', 'localtime', '+3 days'), '10:00', '12:00', 2, 100.0, datetime('now', 'localtime', 'start of day', '-5 days', '+16 hours', '+5 minutes')),
    (5, 'Ana Torres', '**** **** **** 3333', date('now', 'localtime', '-18 days'), '18:00', '19:00', 1, 50.0, datetime('now', 'localtime', 'start of day', '-20 days', '+11 hours', '+35 minutes'));

INSERT INTO SHARE_LISTING (id, seller_id, price_per_share, pack_price, listed_at) VALUES
    (1, (SELECT id FROM SHAREHOLDER WHERE national_id = '11111111H'), 12.50, 230.00, datetime('now', 'localtime', 'start of day', '-6 days', '+10 hours', '+30 minutes')),
    (2, (SELECT id FROM SHAREHOLDER WHERE national_id = '22222222J'), 13.00, NULL, datetime('now', 'localtime', 'start of day', '-4 days', '+17 hours', '+15 minutes')),
    (3, (SELECT id FROM SHAREHOLDER WHERE national_id = '33333333P'), 11.80, 110.00, datetime('now', 'localtime', 'start of day', '-3 days', '+9 hours')),
    (4, (SELECT id FROM SHAREHOLDER WHERE national_id = '22222222J'), 14.90, 70.00, datetime('now', 'localtime', 'start of day', '-1 days', '+12 hours')),
    (5, (SELECT id FROM SHAREHOLDER WHERE national_id = '33333333P'), 15.20, NULL, datetime('now', 'localtime', 'start of day', '+8 hours', '+30 minutes'));

WITH RECURSIVE numbers(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM numbers WHERE n < 3000),
ranges(listing_id, first_id, last_id) AS (
    SELECT 1, 1001, 1020
    UNION ALL SELECT 2, 1301, 1315
    UNION ALL SELECT 3, 301, 310
    UNION ALL SELECT 4, 2001, 2005
    UNION ALL SELECT 5, 2251, 2253
)
INSERT INTO SHARE_LISTING_ITEM (listing_id, share_id)
SELECT r.listing_id, n.n FROM ranges r JOIN numbers n ON n.n BETWEEN r.first_id AND r.last_id;

INSERT INTO SHARE_HOLDING (shareholder_id, campaign_id, purchase_date, purchase_price) VALUES
    ((SELECT id FROM SHAREHOLDER WHERE national_id = '22222222J'), (SELECT id FROM SHARE_CAMPAIGN WHERE name = 'Founding shares 2023'),
     datetime('now', 'localtime', 'start of day', '-1 days', '+19 hours', '+20 minutes'), 11.80);

INSERT INTO SHARE_TRANSFER (purchase_number, listing_id, share_id, seller_id, buyer_id, from_holding_id, to_holding_id, sale_type, price, transferred_at)
SELECT 1, 3, s.id, seller.id, buyer.id, s.holding_id, new_holding.id, 'SINGLE', 11.80, new_holding.purchase_date
FROM SHARE s
JOIN SHAREHOLDER seller ON seller.national_id = '33333333P'
JOIN SHAREHOLDER buyer ON buyer.national_id = '22222222J'
JOIN SHARE_HOLDING new_holding ON new_holding.shareholder_id = buyer.id AND new_holding.purchase_price = 11.80
WHERE s.id IN (301, 302);

UPDATE SHARE SET holding_id = (SELECT to_holding_id FROM SHARE_TRANSFER WHERE share_id = SHARE.id)
WHERE id IN (301, 302);

INSERT INTO GARDENING_SESSION (facility_id, description, date, start_time, end_time, type) VALUES
	(2, 'Pruning trees and trimming hedges',
        '2026-11-11', '08:00', '12:00', 'Exterior'),

    (4, 'Cleaning and maintenance of gym facilities',
        '2026-11-12', '15:00', '17:00', 'Interior'),

    (3, 'Cleaning corridors and common areas',
        '2026-11-13', '09:00', '11:00', 'Interior'),

    (1, 'Garden irrigation system maintenance',
        '2026-11-14', '08:00', '10:00', 'Exterior'),

    (4, 'Removal of leaves and garden debris',
        '2026-11-16', '10:00', '13:00', 'Exterior');

INSERT INTO gardening_session_employee
    (gardening_session_id, employee_id)
VALUES
    -- Session 1
    (1, 7),
	-- Session 2
    (2, 7),
    (2, 8),
    -- Session 3
    (3, 7),
    (3, 8),
    -- Session 4
    (4, 8),
    -- Session 5
    (5, 7);
    
INSERT OR IGNORE INTO FAN (id, dni, full_name, email) VALUES
    (101, '12345678X', 'Carlos González', 'carlos@example.com'),
    (102, '87654321Y', 'María Fernández', 'maria@example.com'),
    (103, '11223344Z', 'Juan Martínez', 'juan@example.com'),
    (104, '44332211K', 'Ana López', 'ana@example.com');

INSERT INTO SEASON_PASS (fan_id, team_id, grandstand, section, row_num, seat_num, age_category, price, start_date, end_date) VALUES
    (101, (SELECT id FROM TEAM WHERE name = 'First Team'), 'A', 'A', 1, 2, 'Adults', 250.00, '2026-07-01', '2027-06-30'),
    (102, (SELECT id FROM TEAM WHERE name = 'First Team'), 'A', 'A', 2, 5, 'Adults', 250.00, '2026-07-01', '2027-06-30'),
    (103, (SELECT id FROM TEAM WHERE name = 'First Team'), 'B', 'C', 4, 8, 'Adults', 220.00, '2026-07-01', '2027-06-30');

-- (2026/27)
INSERT INTO matches (team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) VALUES
    ((SELECT id FROM TEAM WHERE name = 'First Team'), 'Villarreal CF', '2026-11-25', '18:00', '20:00', 1, 0, 1),
    ((SELECT id FROM TEAM WHERE name = 'First Team'), 'Real Madrid', '2026-12-15', '21:00', '23:00', 1, 1, 1);

-- (2027/28)
INSERT INTO matches (team_id, rival, match_date, start_time, end_time, is_home, is_special_match, facility_id) VALUES
    ((SELECT id FROM TEAM WHERE name = 'First Team'), 'Getafe CF', '2027-09-20', '19:00', '21:00', 1, 0, 1),
    ((SELECT id FROM TEAM WHERE name = 'First Team'), 'FC Barcelona', '2027-10-30', '21:00', '23:00', 1, 1, 1);
    
    
INSERT INTO news (id, headline, body_text, image_paths, publication_date, author_id) VALUES
(1, 'Huge victory in the derby (3-0)', 'The first team achieved a resounding 3-0 against our traditional rivals in an unforgettable match at Portman Road. The goals came in the second half after an intense tactical masterclass by the coaching staff. Fans celebrated passionately across the stadium.', 'images/derby1.jpg,images/derby2.jpg', '2026-11-10', 1),

(2, 'New improvements in training facilities', 'Renovation work has started a couple of days ago on the club training pitches and medical recovery center. These state-of-the-art upgrades will ensure our professional and youth academy players have world-class infrastructure for the upcoming seasons.', 'images/training1.png,images/training2.png,images/training3.png,images/training4.jpg', '2026-11-05', 1),

(3, 'Intense prep for upcoming match', 'The coaching staff has designed a week of rigorous tactical sessions and physical conditioning to prepare the squad for the next away fixture. Morale is exceptionally high following the recent victories and support from traveling supporters.', 'images/prep1.jpg,images/prep2.jpg,images/prep3.jpeg,images/prep4.jpg', '2026-11-02', 2);