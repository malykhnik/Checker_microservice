CREATE TABLE endpoints
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role_id  BIGINT REFERENCES roles (id),
    url      VARCHAR(255),
    period   BIGINT
);