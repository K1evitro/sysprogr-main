CREATE TABLE "user" (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL
);

CREATE TABLE role (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permission (
    id         BIGSERIAL PRIMARY KEY,
    resource   VARCHAR(50) NOT NULL,
    operation  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_permission UNIQUE (resource, operation)
);

CREATE TABLE role_permission (
    role_id        BIGINT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_id  BIGINT NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_role (
    user_id  BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE bus (
    id            BIGSERIAL PRIMARY KEY,
    plate_number  VARCHAR(20) NOT NULL UNIQUE,
    route         VARCHAR(100)
);

CREATE TABLE sensor (
    id        BIGSERIAL PRIMARY KEY,
    type      VARCHAR(50) NOT NULL,
    location  VARCHAR(100),
    bus_id    BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE
);

CREATE TABLE sensor_data (
    id         BIGSERIAL PRIMARY KEY,
    sensor_id  BIGINT NOT NULL REFERENCES sensor(id) ON DELETE CASCADE,
    timestamp  TIMESTAMP NOT NULL,
    value      DOUBLE PRECISION NOT NULL,
    anomaly    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE sensor_file (
    id          BIGSERIAL PRIMARY KEY,
    bus_id      BIGINT NOT NULL REFERENCES bus(id) ON DELETE CASCADE,
    file_name   VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT now()
);
