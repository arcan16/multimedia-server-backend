package MultimediaServer.repositories;

import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import MultimediaServer.models.enums.EUserStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // Bloquea las filas de los admins: dos admins que se borran o degradan a la vez no pueden dejar el sistema sin admins.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<UserEntity> findAllByRolAndStatus(ERol rol, EUserStatus status);
}
