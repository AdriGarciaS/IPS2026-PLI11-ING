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

-- ---------------------------------------------------------------------------
-- Facility reservations for external people (US: facility manager).
-- ---------------------------------------------------------------------------

-- Club facilities that can be used by the teams and booked by external people.
CREATE TABLE IF NOT EXISTS FACILITY (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT    NOT NULL UNIQUE
);

-- Periods in which a team of the club uses a facility (trainings, matches...).
-- For now only sample data; another user story will manage these rows.
-- After each use the facility can not be booked for 1 h 30 min.
-- use_date: YYYY-MM-DD, start_time / end_time: HH:MM.
CREATE TABLE IF NOT EXISTS FACILITY_TEAM_USE (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    facility_id INTEGER NOT NULL REFERENCES FACILITY (id),
    team_name   TEXT    NOT NULL,
    use_date    TEXT    NOT NULL,
    start_time  TEXT    NOT NULL,
    end_time    TEXT    NOT NULL,
    CHECK (end_time > start_time)
);

-- One row per reservation made by an external person.
-- card_number: only the masked number is stored (for example
-- "**** **** **** 1111"), never the full card number.
-- total_price: hours x 50 EUR, the money obtained with the reservation.
-- created_at: moment in which the reservation was made (YYYY-MM-DD HH:MM:SS).
CREATE TABLE IF NOT EXISTS FACILITY_RESERVATION (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    facility_id      INTEGER NOT NULL REFERENCES FACILITY (id),
    holder_name      TEXT    NOT NULL,
    card_number      TEXT    NOT NULL,
    reservation_date TEXT    NOT NULL,
    start_time       TEXT    NOT NULL,
    end_time         TEXT    NOT NULL,
    hours            INTEGER NOT NULL CHECK (hours >= 1),
    total_price      REAL    NOT NULL CHECK (total_price >= 0),
    created_at       TEXT    NOT NULL,
    CHECK (end_time > start_time)
);

create table if not exists work_schedule(
	id			integer primary key autoincrement,
	employee_id integer not null,
	week_day 	integer not null,
	start_time	time not null,
	end_time	time not null,
	foreign key (employee_id) references employees(id)
	
);
