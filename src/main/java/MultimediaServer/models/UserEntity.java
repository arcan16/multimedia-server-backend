package MultimediaServer.models;

import MultimediaServer.models.enums.ERol;
import MultimediaServer.models.enums.EUserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// active_username es una columna generada por MySQL (unicidad entre usuarios no borrados): no se mapea.
@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private ERol rol = ERol.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EUserStatus status = EUserStatus.ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Sin FK en el esquema (el primer usuario no tiene creador), por eso se guarda solo el id.
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    // Lado inverso del 1:1 con PERSONS (PK compartida). Puede ser null: el admin inicial no tiene persona.
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private PersonEntity person;
}
