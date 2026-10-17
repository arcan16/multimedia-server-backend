package MultimediaServer.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GENRES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Long id;

    @Column(name = "genre", nullable = false, unique = true, length = 50)
    private String genre;
}
