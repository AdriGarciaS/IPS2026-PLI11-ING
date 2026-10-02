-- Creacion de las tablas de la base de datos.
-- Se ejecuta al arrancar la aplicacion. Usar siempre "IF NOT EXISTS" para que
-- no falle si la base de datos ya existe (modo "mantener").
-- Cada sentencia debe terminar en ";".

CREATE TABLE IF NOT EXISTS personas (
    id     INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    email  TEXT NOT NULL
);

create table if not exists employees (
	id	integer primary key autoincrement,
	type		text not null,
	name		text not null,
	surname		text not null,
	dni			text not null unique,
	birthdate	date not null,	
	salary		decimal(10, 2) not null,
	position	text not null,
	phone		text
);

create table if not exists work_schedule(
	id			integer primary key autoincrement,
	employee_id integer not null,
	week_day 	integer not null,
	start_time	time not null,
	end_time	time not null,
	foreign key (employee_id) references employees(id)
	
);
