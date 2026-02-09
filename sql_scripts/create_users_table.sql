 CREATE TABLE sicurezzaNelleApplicazioni.dbo.users (                                                                                                
     id INT IDENTITY(1,1) NOT NULL,                                                                                                                 
     username NVARCHAR(20) COLLATE Latin1_General_CI_AS NOT NULL,                                                                                   
     email NVARCHAR(128) COLLATE Latin1_General_CI_AS NOT NULL,                                                                                     
     password_hash NVARCHAR(255) COLLATE Latin1_General_CI_AS NOT NULL,                                                                             
     salt NVARCHAR(64) COLLATE Latin1_General_CI_AS NOT NULL,                                                                                       
     full_name NVARCHAR(100) COLLATE Latin1_General_CI_AS NULL,                                                                                     
     failed_attempts INT DEFAULT 0 NULL,                                                                                                            
     lockout_until DATETIME2 NULL,                                                                                                                  
     last_login DATETIME2 NULL, -- Questa è la colonna mancante che causava l'errore!                                                               
     CONSTRAINT PK_users PRIMARY KEY (id),                                                                                                          
     CONSTRAINT UQ_users_username UNIQUE (username),                                                                                                
     CONSTRAINT UQ_users_email UNIQUE (email)                                                                                                       
     );                                           