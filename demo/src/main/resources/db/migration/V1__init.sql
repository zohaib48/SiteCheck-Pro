-- Flyway migration: Initialize schema and seed data for PostgreSQL

-- Table: persistent_logins
CREATE TABLE IF NOT EXISTS persistent_logins (
  series VARCHAR(255) PRIMARY KEY,
  last_used TIMESTAMP(6),
  token VARCHAR(255),
  username VARCHAR(255)
);

-- Table: users
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  website_remaining_limit INTEGER NOT NULL,
  email VARCHAR(255) UNIQUE,
  last_update TIMESTAMP(6),
  name VARCHAR(255),
  password VARCHAR(255),
  remaining_limit INTEGER NOT NULL
);

-- Seed data (upserts to avoid duplicates)
INSERT INTO persistent_logins (series, last_used, token, username) VALUES
('ISi91mtTH3T/nXF+jq8RBA==', '2024-08-06 14:57:24.777000', '/qK9ldxo14Rxy+eZ9Fo/0Q==', 'zohaibshafique66@gmail.com')
ON CONFLICT (series) DO UPDATE SET
  last_used = EXCLUDED.last_used,
  token = EXCLUDED.token,
  username = EXCLUDED.username;

INSERT INTO users (id, website_remaining_limit, email, last_update, name, password, remaining_limit) VALUES
(1, 500, 'arkm684@gmail.com', '2024-02-11 12:05:48.507046', 'ar', '$2a$10$1x1R5ts/SndMJK4DK6ah2utmuSV424/D2S3CpagxDEVNe7o34VVT6', 63)
ON CONFLICT (id) DO UPDATE SET
  website_remaining_limit = EXCLUDED.website_remaining_limit,
  email = EXCLUDED.email,
  last_update = EXCLUDED.last_update,
  name = EXCLUDED.name,
  password = EXCLUDED.password,
  remaining_limit = EXCLUDED.remaining_limit;

INSERT INTO users (id, website_remaining_limit, email, last_update, name, password, remaining_limit) VALUES
(2, 381, 'zohaibshafique48@gmail.com', '2024-08-06 13:49:35.781784', 'zohaib', '$2a$10$mpikLvzvYfZnFBhD8fuXOOEmrnI59jpAQGf2BQZ4o5NbOK5TJ1sG2', 216)
ON CONFLICT (id) DO UPDATE SET
  website_remaining_limit = EXCLUDED.website_remaining_limit,
  email = EXCLUDED.email,
  last_update = EXCLUDED.last_update,
  name = EXCLUDED.name,
  password = EXCLUDED.password,
  remaining_limit = EXCLUDED.remaining_limit;

INSERT INTO users (id, website_remaining_limit, email, last_update, name, password, remaining_limit) VALUES
(3, 500, 'zohaibshafique66@gmail.com', '2024-08-06 14:58:21.152220', 'zohaib shafiq', '$2a$10$cr09hxn9gzX0UXJ1Zsm8C.DXNmtxIQZiakIuyT1wYn4fSUqqHkWea', 455)
ON CONFLICT (id) DO UPDATE SET
  website_remaining_limit = EXCLUDED.website_remaining_limit,
  email = EXCLUDED.email,
  last_update = EXCLUDED.last_update,
  name = EXCLUDED.name,
  password = EXCLUDED.password,
  remaining_limit = EXCLUDED.remaining_limit;

-- Ensure the users ID sequence matches the max id (only if sequence exists)
DO $$
BEGIN
  PERFORM setval(pg_get_serial_sequence('users','id'), (SELECT COALESCE(MAX(id), 0) FROM users), true);
EXCEPTION WHEN others THEN
  -- ignore if sequence not found
  NULL;
END $$;
