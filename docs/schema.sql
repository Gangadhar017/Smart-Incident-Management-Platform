-- ============================================================================
-- DB SCHEMA: AUTH DATABASE (auth_db)
-- ============================================================================

-- Table: Departments
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    manager_id BIGINT,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Table: Teams
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    lead_id BIGINT,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_team_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Table: Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    department_id BIGINT,
    team_id BIGINT,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_user_team FOREIGN KEY (team_id) REFERENCES teams(id)
);

-- Table: User Skills (Element Collection mapping)
CREATE TABLE user_skills (
    user_id BIGINT NOT NULL,
    skill VARCHAR(255) NOT NULL,
    CONSTRAINT fk_skills_user FOREIGN KEY (user_id) REFERENCES users(id),
    PRIMARY KEY (user_id, skill)
);

-- Add index on username and email for O(1) authentications
CREATE INDEX idx_user_auth ON users (username, email);


-- ============================================================================
-- DB SCHEMA: INCIDENT DATABASE (incident_db)
-- ============================================================================

-- Table: SLA Rules
CREATE TABLE sla_rules (
    id BIGSERIAL PRIMARY KEY,
    priority VARCHAR(10) NOT NULL UNIQUE,
    response_time_minutes BIGINT NOT NULL,
    resolution_time_minutes BIGINT NOT NULL
);

-- Seed initial rules
INSERT INTO sla_rules (priority, response_time_minutes, resolution_time_minutes) VALUES
