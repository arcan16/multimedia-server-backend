package MultimediaServer.repositories;

import MultimediaServer.models.PersonEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    Page<PersonEntity> findAllByDeletedAtIsNull(Pageable page);

    Optional<PersonEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(String email, Long id);
}
