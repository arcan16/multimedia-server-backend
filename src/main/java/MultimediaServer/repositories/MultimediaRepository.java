package MultimediaServer.repositories;

import MultimediaServer.models.MultimediaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MultimediaRepository extends JpaRepository<MultimediaEntity, Long> {

    Page<MultimediaEntity> findAllByDeletedAtIsNull(Pageable page);

    Optional<MultimediaEntity> findByIdAndDeletedAtIsNull(Long id);

    Page<MultimediaEntity> findAllByMovieSeries_IdAndDeletedAtIsNull(Long movieSeriesId, Pageable page);

    // Borrado en cascada al eliminar su MovieSeries.
    List<MultimediaEntity> findAllByMovieSeries_IdAndDeletedAtIsNull(Long movieSeriesId);

    boolean existsByMovieSeries_IdAndDeletedAtIsNull(Long movieSeriesId);

    // Solo activos: desde V3, uk_multimedia_active_episode ignora los registros borrados lógicamente.
    boolean existsByMovieSeries_IdAndSeasonAndEpisodeNumberAndDeletedAtIsNull(Long movieSeriesId, Integer season, Integer episodeNumber);

    boolean existsByMovieSeries_IdAndSeasonAndEpisodeNumberAndDeletedAtIsNullAndIdNot(Long movieSeriesId, Integer season, Integer episodeNumber, Long id);
}
