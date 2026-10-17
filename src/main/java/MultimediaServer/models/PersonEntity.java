package MultimediaServer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// active_email es una columna generada por MySQL (unicidad entre personas no borradas): no se mapea.
@Entity
@Table(name = "PERSONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonEntity {

    @Id
    @Column(name = "person_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private UserEntity user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "lastname", nullable = false, length = 50)
    private String lastname;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private UserEntity deletedBy;
}
