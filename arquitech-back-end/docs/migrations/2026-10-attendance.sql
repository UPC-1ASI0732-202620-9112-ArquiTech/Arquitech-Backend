-- MySQL 8. Use only when schema changes are managed manually (DDL_AUTO=validate/none).
-- With the existing DDL_AUTO=update, Hibernate creates this table at startup.
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    worker_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    check_in_at DATETIME(6) NULL,
    check_out_at DATETIME(6) NULL,
    notes VARCHAR(1000) NULL,
    registered_by_user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_attendance_worker_date UNIQUE (worker_id, attendance_date),
    KEY idx_attendance_project_date (project_id, attendance_date),
    CONSTRAINT fk_attendance_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_attendance_worker FOREIGN KEY (worker_id) REFERENCES workers(id),
    CONSTRAINT fk_attendance_user FOREIGN KEY (registered_by_user_id) REFERENCES users(id)
);
