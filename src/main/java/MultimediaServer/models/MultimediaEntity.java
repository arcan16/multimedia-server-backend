package MultimediaServer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// active_flag es una columna generada por MySQL (unicidad de episodios entre registros no borrados, V3): no se mapea.
@Entity
@Table(name = "MULTIMEDIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "multimedia_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movies_series_id", nullable = false)
    private MovieSeriesEntity movieSeries;

    @Column(name = "multimedia_path", nullable = false)
    private String multimediaPath;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "season")
    private Integer season;

    @Column(name = "episode_number")
    private Integer episodeNumber;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

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
