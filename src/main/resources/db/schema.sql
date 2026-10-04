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
-- Merchandising products sold in the club store.
-- available_units: number of units in stock (a sale can never exceed it).
CREATE TABLE IF NOT EXISTS MERCHANDISING (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    type            TEXT    NOT NULL,
    available_units INTEGER NOT NULL CHECK (available_units >= 0),
    price           REAL    NOT NULL CHECK (price >= 0)
);

-- One row per product line of a purchase. All the lines of the same purchase
-- share the same sale_number, so the final price of a purchase is
-- SUM(total_price) grouped by sale_number.
-- total_price: units_sold x unit price at the moment of the sale.
-- sale_date: date of the sale in ISO format (YYYY-MM-DD).
CREATE TABLE IF NOT EXISTS MERCHANDISING_SALE (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_number INTEGER NOT NULL,
    merch_id    INTEGER NOT NULL REFERENCES MERCHANDISING (id),
    units_sold  INTEGER NOT NULL CHECK (units_sold > 0),
    total_price REAL    NOT NULL CHECK (total_price >= 0),
    sale_date   TEXT    NOT NULL
);
