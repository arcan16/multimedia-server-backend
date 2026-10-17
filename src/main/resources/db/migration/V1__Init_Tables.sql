CREATE TABLE USERS (
    user_id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,

    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,

    rol ENUM('ADMIN','USER') DEFAULT 'USER' NOT NULL,
    status ENUM('ACTIVE','INACTIVE','DELETED') DEFAULT 'ACTIVE' NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,

    active_username VARCHAR(50)
        GENERATED ALWAYS AS (
            IF(status = 'DELETED', NULL, username)
            ) STORED,

    UNIQUE KEY uq_users_active_username (active_username)
) ENGINE=InnoDB;

CREATE TABLE PERSONS (
    person_id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,

    active_email VARCHAR(100)
        GENERATED ALWAYS AS (IF(deleted_at IS NULL, email, NULL)) STORED,
    UNIQUE KEY uq_persons_active_email (active_email),

    CONSTRAINT fk_persons_id_user_id FOREIGN KEY (person_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_persons_created_by_user_id FOREIGN KEY (created_by) REFERENCES USERS(user_id),
    CONSTRAINT fk_persons_deleted_by_user_id FOREIGN KEY (deleted_by) REFERENCES USERS(user_id)
)ENGINE=InnoDB;

CREATE TABLE MOVIES_SERIES (
    movies_series_id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    synopsis TEXT NOT NULL,
    poster_path VARCHAR(255),
    release_year YEAR NOT NULL,
    classification VARCHAR(10),
    type ENUM('MOVIE','SERIE') NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,

    CONSTRAINT fk_movies_series_created_by_user_id FOREIGN KEY (created_by) REFERENCES USERS(user_id),
    CONSTRAINT fk_movies_series_deleted_by_user_id FOREIGN KEY (deleted_by) REFERENCES USERS(user_id)
)ENGINE=InnoDB;

CREATE TABLE GENRES (
    genre_id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    genre VARCHAR(50) NOT NULL UNIQUE
)ENGINE=InnoDB;

CREATE TABLE MULTIMEDIA (
    multimedia_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    movies_series_id BIGINT NOT NULL,
    multimedia_path VARCHAR(255) NOT NULL,
    title VARCHAR(100),
    season SMALLINT UNSIGNED,
    episode_number SMALLINT UNSIGNED,
    duration_seconds     INT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by BIGINT,

    CONSTRAINT fk_multimedia_created_by_user_id FOREIGN KEY (created_by) REFERENCES USERS(user_id),
    CONSTRAINT fk_multimedia_deleted_by_user_id FOREIGN KEY (deleted_by) REFERENCES USERS(user_id),
    CONSTRAINT fk_multimedia_movies_series FOREIGN KEY (movies_series_id) REFERENCES MOVIES_SERIES(movies_series_id),
    CONSTRAINT uk_multimedia_episode UNIQUE (movies_series_id, season, episode_number)

)ENGINE=InnoDB;

CREATE TABLE MOVIES_SERIES_GENRES(
    movies_series_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (movies_series_id, genre_id),

    CONSTRAINT fk_msg_movies_series FOREIGN KEY (movies_series_id) REFERENCES MOVIES_SERIES(movies_series_id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_genre FOREIGN KEY (genre_id) REFERENCES GENRES(genre_id)
) ENGINE=InnoDB;

CREATE TABLE AUDIT_LOG(
    audit_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(60) NOT NULL,
    record_id BIGINT NOT NULL,
    action ENUM('INSERT','UPDATE','DELETE') NOT NULL,

    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    old_values JSON,
    new_values JSON,

    user_id BIGINT NOT NULL,
    user_snapshot VARCHAR(200) NOT NULL,
    index(table_name, record_id),

    CONSTRAINT fk_audit_log_user_id_user_id FOREIGN KEY (user_id) REFERENCES USERS(user_id)
)ENGINE=InnoDB;
