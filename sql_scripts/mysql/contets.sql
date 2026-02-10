
CREATE TABLE contents (
    id CHAR(36) NOT NULL,
    user_id INT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    internal_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY UQ_contents_internal_name (internal_name),
    CONSTRAINT FK_contents_users FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
