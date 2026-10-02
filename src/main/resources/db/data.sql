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