-- Smart Emergency Response System Database Schema

CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT,
    user_code VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY (user_code),
    UNIQUE KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS emergencies (
    emergency_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    emergency_type VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    affected_people INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (emergency_id),
    KEY (user_id),
    CONSTRAINT fk_emergencies_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS resources (
    resource_id INT NOT NULL AUTO_INCREMENT,
    resource_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    resource_status VARCHAR(30) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    patient_capacity INT DEFAULT NULL,
    water_capacity INT DEFAULT NULL,
    team_members INT DEFAULT NULL,
    PRIMARY KEY (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT NOT NULL AUTO_INCREMENT,
    emergency_id INT NOT NULL,
    resource_id INT NOT NULL,
    assigned_at DATETIME NOT NULL,
    released_at DATETIME DEFAULT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (allocation_id),
    KEY (emergency_id),
    KEY (resource_id),
    CONSTRAINT fk_allocations_emergency FOREIGN KEY (emergency_id) REFERENCES emergencies (emergency_id),
    CONSTRAINT fk_allocations_resource FOREIGN KEY (resource_id) REFERENCES resources (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    emergency_id INT NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (notification_id),
    KEY (user_id),
    KEY (emergency_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_notifications_emergency FOREIGN KEY (emergency_id) REFERENCES emergencies (emergency_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS emergency_status_history (
    history_id INT NOT NULL AUTO_INCREMENT,
    emergency_id INT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_at DATETIME NOT NULL,
    PRIMARY KEY (history_id),
    KEY (emergency_id),
    CONSTRAINT fk_status_history_emergency FOREIGN KEY (emergency_id) REFERENCES emergencies (emergency_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
