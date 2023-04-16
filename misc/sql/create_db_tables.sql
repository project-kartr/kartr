DROP TABLE IF EXISTS file;
DROP TABLE IF EXISTS story;
DROP TABLE IF EXISTS poi;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
	id SERIAL PRIMARY KEY,
	email TEXT NOT NULL UNIQUE,
	password TEXT NOT NULL,
	displayname TEXT NOT NULL,
	is_admin BOOLEAN NOT NULL DEFAULT FALSE,
	is_active BOOLEAN DEFAULT false,
	register_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
	reset_password_token TEXT
);

CREATE TABLE poi (
	id SERIAL PRIMARY KEY,
	longitude decimal(9,6) NOT NULL,
	latitude decimal(9,6) NOT NULL,
	displayname TEXT NOT NULL,
	account_id integer REFERENCES account(id)
);

CREATE TABLE story (
	id SERIAL PRIMARY KEY,
	headline TEXT,
	content TEXT,
	account_id integer REFERENCES account(id),
	poi_id integer REFERENCES poi(id)
);

CREATE TABLE file (
	filename TEXT PRIMARY KEY,
	story_id integer REFERENCES story(id),
	mime_type TEXT
);
