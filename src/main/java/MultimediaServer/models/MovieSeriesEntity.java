package MultimediaServer.models;

import MultimediaServer.models.enums.EMovieSeriesType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "MOVIES_SERIES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieSeriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movies_series_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "synopsis", nullable = false, columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "poster_path")
    private String posterPath;

    // Columna YEAR de MySQL.
    @Column(name = "release_year", nullable = false)
    private Short releaseYear;

    @Column(name = "classification", length = 10)
    private String classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EMovieSeriesType type;

    // Tabla puente MOVIES_SERIES_GENRES.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "MOVIES_SERIES_GENRES",
            joinColumns = @JoinColumn(name = "movies_series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<GenreEntity> genres = new HashSet<>();

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
