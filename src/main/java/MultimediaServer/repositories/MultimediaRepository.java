package MultimediaServer.repositories;

import MultimediaServer.models.MultimediaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MultimediaRepository extends JpaRepository<MultimediaEntity, Long> {

    Page<MultimediaEntity> findAllByDeletedAtIsNull(Pageable page);

    Optional<MultimediaEntity> findByIdAndDeletedAtIsNull(Long id);

    Page<MultimediaEntity> findAllByMovieSeries_IdAndDeletedAtIsNull(Long movieSeriesId, Pageable page);

    // Sin filtrar deletedAt: uk_multimedia_episode también aplica a registros borrados lógicamente.
    boolean existsByMovieSeries_IdAndSeasonAndEpisodeNumber(Long movieSeriesId, Integer season, Integer episodeNumber);

    boolean existsByMovieSeries_IdAndSeasonAndEpisodeNumberAndIdNot(Long movieSeriesId, Integer season, Integer episodeNumber, Long id);
}
