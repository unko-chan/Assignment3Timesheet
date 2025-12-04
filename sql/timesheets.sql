-- ============================================================================
-- COMP3910 Assignment 3 - Timesheet Service Schema
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `timesheets`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `timesheets`;

-- ============================================================================
-- TABLE: users
-- Notes:
--   - "user" is a reserved word in some DBs, so table is named "users".
--   - Stores login credentials and basic profile info.
-- ============================================================================

CREATE TABLE users (
                       id               INT UNSIGNED NOT NULL AUTO_INCREMENT,
                       username         VARCHAR(64)  NOT NULL,
                       password    VARCHAR(255) NOT NULL,
                       first_name       VARCHAR(100) NOT NULL,
                       last_name        VARCHAR(100) NOT NULL,
                       employee_number  INT UNSIGNED NOT NULL,
                       role             ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
                       active           TINYINT(1)   NOT NULL DEFAULT 1,
                       created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT pk_users PRIMARY KEY (id),
                       CONSTRAINT uq_users_username UNIQUE (username),
                       CONSTRAINT uq_users_employee_number UNIQUE (employee_number)
)
    ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: auth_tokens
-- Notes:
--   - Stores login tokens issued to users.
--   - Tokens are short-lived and must be validated on each request.
-- ============================================================================

CREATE TABLE auth_tokens (
                             token       VARCHAR(64)      NOT NULL,
                             user_id     INT UNSIGNED  NOT NULL,
                             issued_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             expires_at  DATETIME      NOT NULL,
                             active      TINYINT(1)    NOT NULL DEFAULT 1,

                             CONSTRAINT pk_auth_tokens PRIMARY KEY (token),

                             CONSTRAINT fk_auth_tokens_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users (id)
                                     ON DELETE CASCADE
                                     ON UPDATE CASCADE
)
    ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_auth_tokens_user_id
    ON auth_tokens (user_id);

-- ============================================================================
-- TABLE: timesheets
-- Notes:
--   - Represents a weekly timesheet for a given user.
--   - One timesheet per user per week (user_id + week_start must be unique).
-- ============================================================================

CREATE TABLE timesheets (
                            id           INT UNSIGNED NOT NULL AUTO_INCREMENT,
                            user_id      INT UNSIGNED NOT NULL,
                            week_start   DATE         NOT NULL,     -- e.g., Monday of the week
                            status       ENUM('OPEN','SUBMITTED','APPROVED') NOT NULL DEFAULT 'OPEN',
                            total_hours  DECIMAL(5,2) NOT NULL DEFAULT 0.00,
                            created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,

                            CONSTRAINT pk_timesheets PRIMARY KEY (id),

                            CONSTRAINT fk_timesheets_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users (id)
                                    ON DELETE RESTRICT
                                    ON UPDATE CASCADE,

                            CONSTRAINT uq_timesheets_user_week
                                UNIQUE (user_id, week_start)
)
    ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_timesheets_user_id
    ON timesheets (user_id);

CREATE INDEX idx_timesheets_week_start
    ON timesheets (week_start);

-- Optional CHECKs (effective in MySQL 8+; ignored in older versions)
-- ALTER TABLE timesheets
--   ADD CONSTRAINT chk_timesheets_total_hours
--   CHECK (total_hours >= 0.00 AND total_hours <= 168.00);

-- ============================================================================
-- TABLE: timesheet_entries
-- Notes:
--   - Individual work entries belonging to a timesheet.
--   - Each entry must be within the parent timesheet’s week in application logic.
-- ============================================================================

CREATE TABLE timesheet_entries (
                                   id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
                                   timesheet_id  INT UNSIGNED NOT NULL,
                                   work_date     DATE         NOT NULL,
                                   project_code  VARCHAR(50)  NOT NULL,
                                   task_code     VARCHAR(50)  DEFAULT NULL,
                                   hours         DECIMAL(4,2) NOT NULL,
                                   description   VARCHAR(255) DEFAULT NULL,

                                   CONSTRAINT pk_timesheet_entries PRIMARY KEY (id),

                                   CONSTRAINT fk_entries_timesheet
                                       FOREIGN KEY (timesheet_id)
                                           REFERENCES timesheets (id)
                                           ON DELETE CASCADE
                                           ON UPDATE CASCADE
)
    ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_entries_timesheet_id
    ON timesheet_entries (timesheet_id);

CREATE INDEX idx_entries_work_date
    ON timesheet_entries (work_date);

-- Optional CHECKs (MySQL 8+)
-- ALTER TABLE timesheet_entries
--   ADD CONSTRAINT chk_entries_hours
--   CHECK (hours >= 0.00 AND hours <= 24.00);

-- ============================================================================
-- INITIAL DATA (optional but helpful for testing)
-- Creates a default admin user with a placeholder password hash.
-- Replace 'admin' and 'admin-hash' with a real hashed password before deployment.
-- ============================================================================

INSERT INTO users (
    username,
    password,
    first_name,
    last_name,
    employee_number,
    role,
    active
) VALUES (
             'admin',
             'admin',
             'System',
             'Administrator',
             1,
             'ADMIN',
             1
         );

-- You can also insert some sample regular users and timesheets for testing.

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================
