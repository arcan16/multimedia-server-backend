package MultimediaServer.infra.services;

import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.models.AuditLogEntity;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EAuditAction;
import MultimediaServer.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // oldValues/newValues: JSON ya serializado del Full<Entidad>DTO (null en old para INSERT y en new para DELETE).
    public void create(HttpServletRequest request, EAuditAction action, String tableName, Long recordId,
                       String oldValues, String newValues) {
        create(jwtUtils.getUserFromRequest(request), action, tableName, recordId, oldValues, newValues);
    }

    // Variante con el usuario ya resuelto: evita volver a buscarlo por el token
    // (p. ej. cuando un usuario se borra a sí mismo y ya no estaría ACTIVE).
    public void create(UserEntity user, EAuditAction action, String tableName, Long recordId,
                       String oldValues, String newValues) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setAction(action);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setUser(user);
        auditLog.setUserSnapshot(user.getUsername());

        auditLogRepository.save(auditLog);
    }
}
