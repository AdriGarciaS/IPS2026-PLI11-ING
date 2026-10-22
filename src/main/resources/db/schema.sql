 -- Creacion de las tablas de la base de datos.
-- Se ejecuta al arrancar la aplicacion. Usar siempre "IF NOT EXISTS" para que
-- no falle si la base de datos ya existe (modo "mantener").
-- Cada sentencia debe terminar en ";".

CREATE TABLE IF NOT EXISTS employees (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name           TEXT NOT NULL,
    last_name            TEXT NOT NULL,
    national_id          TEXT NOT NULL UNIQUE,
    birth_date           TEXT NOT NULL, -- Format: YYYY-MM-DD
    phone_number         TEXT NOT NULL,
    category             TEXT NOT NULL, -- 'SPORTS' or 'NON_SPORTS'
    position             TEXT NOT NULL,
    gross_annual_salary  REAL NOT NULL,
    gender               TEXT NOT NULL CHECK (gender IN ('MALE', 'FEMALE'))
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
	start_time	text not null,
	end_time	text not null,
	foreign key (employee_id) references employees(id)
	
);

-- Table: interview_slots
CREATE TABLE IF NOT EXISTS interview_slots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    coach_id INTEGER NOT NULL,
    slot_date TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'AVAILABLE',
    media_name TEXT,
    media_contact TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES employees(id),
    FOREIGN KEY (coach_id) REFERENCES employees(id)
);

-- Table: interviews (Assigned / Booked sessions)
CREATE TABLE IF NOT EXISTS interviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    slot_id INTEGER NOT NULL UNIQUE,
    player_id INTEGER NOT NULL,
    interviewer_name TEXT NOT NULL,
    media_outlet TEXT,
    status TEXT NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'COMPLETED', 'CANCELLED'
    FOREIGN KEY (slot_id) REFERENCES interview_slots(id),
    FOREIGN KEY (player_id) REFERENCES employees(id)
);

CREATE TABLE IF NOT EXISTS matches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER,               -- Can be team reference
    match_date TEXT NOT NULL,        -- YYYY-MM-DD
    start_time TEXT NOT NULL,        -- HH:MM
    end_time TEXT NOT NULL,          -- HH:MM
    rival TEXT,
    is_home INTEGER NOT NULL CHECK (is_home IN (0, 1)), -- 1 = in our field, 0 = in the field of the rival
    facility_id INTEGER,             -- facility used, only for home matches
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (facility_id) REFERENCES FACILITY(id),
    CHECK ((is_home = 1 AND facility_id IS NOT NULL) OR (is_home = 0 AND facility_id IS NULL))
);

-- Scheduled training sessions.
-- Trainings and home matches are the uses of the facilities by our teams:
-- after each one, the facility can not be booked for 1 h 30 min.
CREATE TABLE IF NOT EXISTS trainings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER,
    training_date TEXT NOT NULL,     -- YYYY-MM-DD
    start_time TEXT NOT NULL,        -- HH:MM
    end_time TEXT NOT NULL,          -- HH:MM
    facility_id INTEGER NOT NULL,    -- facility where the training takes place
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (facility_id) REFERENCES FACILITY(id)
);

create table if not exists day_schedule(
    id              	integer primary key autoincrement,
    work_schedule_id 	integer not null,
    date           		text not null,
    start_time      	text not null,
    end_time        	text not null,

	unique(work_schedule_id, date, start_time, end_time)
    foreign key (work_schedule_id) references work_schedule(id)
);

-- ---------------------------------------------------------------------------
-- Sports teams (US: general manager adds sports teams).
-- ---------------------------------------------------------------------------

-- category: FIRST_TEAM or SUBSIDIARY (professional teams) or JUVENIL, CADETE,
-- INFANTIL, ALEVIN, BENJAMIN, PREBENJAMIN (youth teams).
-- gender: MALE or FEMALE (the players must have the gender of the team).
-- first_coach_id / second_coach_id: mandatory, technical sports employees.
-- created_at: YYYY-MM-DD HH:MM:SS.
CREATE TABLE IF NOT EXISTS TEAM (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    category        TEXT    NOT NULL CHECK (category IN ('FIRST_TEAM', 'SUBSIDIARY', 'JUVENIL', 'CADETE',
                                                         'INFANTIL', 'ALEVIN', 'BENJAMIN', 'PREBENJAMIN')),
    gender          TEXT    NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    first_coach_id  INTEGER NOT NULL REFERENCES employees (id),
    second_coach_id INTEGER NOT NULL REFERENCES employees (id),
    created_at      TEXT    NOT NULL,
    CHECK (first_coach_id <> second_coach_id)
);

-- Players and other technical staff of each team (the two coaches are in TEAM).
-- role: PLAYER or STAFF; task: what a STAFF member does (for example "Goalkeepers").
CREATE TABLE IF NOT EXISTS TEAM_MEMBER (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id     INTEGER NOT NULL REFERENCES TEAM (id),
    employee_id INTEGER NOT NULL REFERENCES employees (id),
    role        TEXT    NOT NULL CHECK (role IN ('PLAYER', 'STAFF')),
    task        TEXT,
    UNIQUE (team_id, employee_id)
);

-- A player can only belong to one team.
CREATE UNIQUE INDEX IF NOT EXISTS ux_team_member_one_team_per_player ON TEAM_MEMBER (employee_id) WHERE role = 'PLAYER';

-- ---------------------------------------------------------------------------
-- Shareholders (US 35747: shares wallet).
-- ---------------------------------------------------------------------------

-- People that own shares of the club. There is no login: a shareholder
-- identifies with the national ID or is searched by name.
CREATE TABLE IF NOT EXISTS SHAREHOLDER (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name  TEXT    NOT NULL,
    last_name   TEXT    NOT NULL,
    national_id TEXT    NOT NULL UNIQUE COLLATE NOCASE
);
