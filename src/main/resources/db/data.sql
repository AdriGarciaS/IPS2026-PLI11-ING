-- Datos de ejemplo (dummy data).
-- Solo se cargan cuando la base de datos se crea desde cero, para no
-- duplicar filas si se mantiene entre ejecuciones.
-- Cada sentencia debe terminar en ";".



INSERT INTO personas (nombre, email) VALUES
    ('Ada Lovelace', 'ada@example.com'),
    ('Alan Turing', 'alan@example.com'),
    ('Peter Parker', 'peter@example.com'),
    ('Grace Hopper', 'grace@example.com');
