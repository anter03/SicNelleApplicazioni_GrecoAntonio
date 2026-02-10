CREATE TABLE sicurezzaNelleApplicazioni.dbo.contents (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id INT NOT NULL,
    original_name NVARCHAR(255) NOT NULL,
    internal_name NVARCHAR(255) NOT NULL UNIQUE,
    mime_type NVARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path NVARCHAR(MAX) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_contents_users FOREIGN KEY (user_id) REFERENCES sicurezzaNelleApplicazioni.dbo.users (id)
);