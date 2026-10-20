package MultimediaServer.repositories;

import MultimediaServer.models.MovieSeriesEntity;
import MultimediaServer.models.enums.EMovieSeriesType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieSeriesRepository extends JpaRepository<MovieSeriesEntity, Long> {

    Optional<MovieSeriesEntity> findByIdAndDeletedAtIsNull(Long id);

    // Filtros opcionales del catálogo: un parámetro null no filtra. EXISTS en vez de JOIN para no duplicar filas al paginar.
    @Query("""
            SELECT m FROM MovieSeriesEntity m
            WHERE m.deletedAt IS NULL
              AND (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:type IS NULL OR m.type = :type)
              AND (:genreId IS NULL OR EXISTS (SELECT g.id FROM m.genres g WHERE g.id = :genreId))
            """)
    Page<MovieSeriesEntity> search(String name, EMovieSeriesType type, Long genreId, Pageable page);

    // Incluye borrados lógicos: la FK de MOVIES_SERIES_GENRES impide borrar un género asociado a cualquier registro.
    boolean existsByGenres_Id(Long genreId);
}
