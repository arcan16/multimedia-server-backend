package MultimediaServer.repositories;

import MultimediaServer.models.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {

    boolean existsByGenreIgnoreCase(String genre);

    boolean existsByGenreIgnoreCaseAndIdNot(String genre, Long id);
}
