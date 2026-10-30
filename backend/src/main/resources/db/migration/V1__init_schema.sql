CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    role        VARCHAR(20)   NOT NULL,
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_role CHECK (role IN ('SUPER_ADMIN', 'USER'))
);

CREATE TABLE user_permissions (
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission  VARCHAR(20)  NOT NULL,
    PRIMARY KEY (user_id, permission),
    CONSTRAINT chk_permission CHECK (permission IN ('CREATE', 'READ', 'UPDATE', 'DELETE'))
);

CREATE TABLE employees (
    id               BIGSERIAL PRIMARY KEY,
    first_name       VARCHAR(100)   NOT NULL,
    last_name        VARCHAR(100)   NOT NULL,
    email            VARCHAR(150)   NOT NULL UNIQUE,
    phone            VARCHAR(30),
    job_title        VARCHAR(100)   NOT NULL,
    department       VARCHAR(100),
    hire_date        DATE           NOT NULL,
    salary           NUMERIC(12, 2),
    photo_key        VARCHAR(255),
    cv_key           VARCHAR(255),
    cv_file_name     VARCHAR(255),
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_name        ON users (LOWER(name));
CREATE INDEX idx_employees_name    ON employees (LOWER(last_name), LOWER(first_name));
CREATE INDEX idx_employees_dept    ON employees (LOWER(department));
