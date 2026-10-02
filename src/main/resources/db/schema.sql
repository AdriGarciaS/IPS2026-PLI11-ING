-- Creacion de las tablas de la base de datos.
-- Se ejecuta al arrancar la aplicacion. Usar siempre "IF NOT EXISTS" para que
-- no falle si la base de datos ya existe (modo "mantener").
-- Cada sentencia debe terminar en ";".

CREATE TABLE IF NOT EXISTS personas (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email  TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS employees (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name           TEXT NOT NULL,
    last_name            TEXT NOT NULL,
    national_id          TEXT NOT NULL UNIQUE,
    birth_date           TEXT NOT NULL, -- Format: YYYY-MM-DD
    phone_number         TEXT NOT NULL,
    category             TEXT NOT NULL, -- 'SPORTS' or 'NON_SPORTS'
    position             TEXT NOT NULL,
    gross_annual_salary  REAL NOT NULL
);