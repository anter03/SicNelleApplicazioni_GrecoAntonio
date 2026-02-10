
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100),
    failed_attempts INT DEFAULT 0,
    lockout_until DATETIME(6),
    last_login DATETIME(6),

    PRIMARY KEY (id),
    UNIQUE KEY UQ_users_username (username),
    UNIQUE KEY UQ_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
