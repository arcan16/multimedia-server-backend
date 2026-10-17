package MultimediaServer.models;

import MultimediaServer.models.enums.EAuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long id;

    @Column(name = "table_name", nullable = false, length = 60)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private EAuditAction action;

    @Column(name = "changed_at", insertable = false, updatable = false)
    private LocalDateTime changedAt;

    // JSON ya serializado (ObjectMapper.writeValueAsString).
    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "user_snapshot", nullable = false, length = 200)
    private String userSnapshot;
}
