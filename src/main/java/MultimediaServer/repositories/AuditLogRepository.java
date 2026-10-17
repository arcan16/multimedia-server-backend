package MultimediaServer.repositories;

import MultimediaServer.models.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    Page<AuditLogEntity> findAllByTableNameAndRecordId(String tableName, Long recordId, Pageable page);
}
