package MultimediaServer.repositories;

import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EUserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// El borrado lógico de USERS se marca con status = DELETED: se pasa EUserStatus.DELETED a los métodos "StatusNot".
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Page<UserEntity> findAllByStatusNot(EUserStatus status, Pageable page);

    Optional<UserEntity> findByIdAndStatusNot(Long id, EUserStatus status);

    // Login / UserDetailsService.
    Optional<UserEntity> findByUsernameAndStatus(String username, EUserStatus status);

    Optional<UserEntity> findByUsernameAndStatusNot(String username, EUserStatus status);

    boolean existsByUsernameAndStatusNot(String username, EUserStatus status);

    boolean existsByUsernameAndStatusNotAndIdNot(String username, EUserStatus status, Long id);

    Optional<UserEntity> findByUsername(String username);
}
