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
('P1', 15, 60),    -- Response: 15m, Resolution: 1h
('P2', 30, 240),   -- Response: 30m, Resolution: 4h
('P3', 120, 1440), -- Response: 2h,  Resolution: 24h
('P4', 480, 4320); -- Response: 8h,  Resolution: 72h

-- Table: Incidents
CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,
    incident_number VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    subcategory VARCHAR(50),
    priority VARCHAR(10) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    assignee_id BIGINT,
    reporter_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP,
    resolved_date TIMESTAMP,
    closed_date TIMESTAMP,
    sla_due_date TIMESTAMP,
    sla_breached BOOLEAN NOT NULL DEFAULT FALSE,
    escalated BOOLEAN NOT NULL DEFAULT FALSE,
    escalation_level INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0
);

-- Indexes for performance filtering and pagination on incident boards
CREATE INDEX idx_incident_filter ON incidents (status, priority, assignee_id);
CREATE INDEX idx_incident_number_lookup ON incidents (incident_number);

-- Table: Comments
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comment_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_incident_id ON comments (incident_id);

-- Table: Attachments
CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    s3_key VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    uploaded_by BIGINT NOT NULL,
    uploaded_date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_attachment_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
);

-- Table: Audit Logs
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    changed_by BIGINT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_incident_id ON audit_logs (incident_id);
