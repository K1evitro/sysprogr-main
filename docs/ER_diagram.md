erDiagram
    USER {
        BIGINT id
        VARCHAR username
        VARCHAR password
    }

    ROLE {
        BIGINT id
        VARCHAR name
    }

    PERMISSION {
        BIGINT id
        VARCHAR resource
        VARCHAR operation
    }

    ROLE_PERMISSION {
        BIGINT role_id
        BIGINT permission_id
    }

    USER_ROLE {
        BIGINT user_id
        BIGINT role_id
    }

    BUS {
        BIGINT id
        VARCHAR plate_number
        VARCHAR route
    }

    SENSOR {
        BIGINT id
        VARCHAR type
        VARCHAR location
        BIGINT bus_id
    }

    SENSOR_DATA {
        BIGINT id
        BIGINT sensor_id
        TIMESTAMP timestamp
        DOUBLE value
        BOOLEAN anomaly
    }

    SENSOR_FILE {
        BIGINT id
        BIGINT bus_id
        VARCHAR file_name
        TIMESTAMP uploaded_at
    }

    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : has
    ROLE ||--o{ ROLE_PERMISSION : has
    PERMISSION ||--o{ ROLE_PERMISSION : includes

    BUS ||--o{ SENSOR : has
    SENSOR ||--o{ SENSOR_DATA : produces
    BUS ||--o{ SENSOR_FILE : has
