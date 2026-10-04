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
