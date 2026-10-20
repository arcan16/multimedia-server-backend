-- uk_multimedia_episode también aplicaba a los registros borrados lógicamente: un episodio borrado
-- impedía volver a subir esa temporada/episodio. Mismo patrón que USERS.active_username y PERSONS.active_email:
-- active_flag vale NULL en los borrados y MySQL no compara NULLs en un UNIQUE.
ALTER TABLE MULTIMEDIA
    ADD COLUMN active_flag TINYINT
        GENERATED ALWAYS AS (IF(deleted_at IS NULL, 1, NULL)) STORED;

-- Se crea antes de borrar el índice antiguo: empieza por movies_series_id, así que sigue sirviendo
-- de índice para fk_multimedia_movies_series.
ALTER TABLE MULTIMEDIA
    ADD CONSTRAINT uk_multimedia_active_episode UNIQUE (movies_series_id, season, episode_number, active_flag);

ALTER TABLE MULTIMEDIA
    DROP INDEX uk_multimedia_episode;
