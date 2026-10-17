package MultimediaServer.repositories;

import MultimediaServer.models.MovieSeriesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieSeriesRepository extends JpaRepository<MovieSeriesEntity, Long> {

    Page<MovieSeriesEntity> findAllByDeletedAtIsNull(Pageable page);

    Optional<MovieSeriesEntity> findByIdAndDeletedAtIsNull(Long id);

    Page<MovieSeriesEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNull(String name, Pageable page);

    // Incluye borrados lógicos: la FK de MOVIES_SERIES_GENRES impide borrar un género asociado a cualquier registro.
    boolean existsByGenres_Id(Long genreId);
}
