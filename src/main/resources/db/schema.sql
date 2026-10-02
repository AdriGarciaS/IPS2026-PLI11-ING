-- Creacion de las tablas de la base de datos.
-- Se ejecuta al arrancar la aplicacion. Usar siempre "IF NOT EXISTS" para que
-- no falle si la base de datos ya existe (modo "mantener").
-- Cada sentencia debe terminar en ";".

CREATE TABLE IF NOT EXISTS personas (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email  TEXT NOT NULL
);
