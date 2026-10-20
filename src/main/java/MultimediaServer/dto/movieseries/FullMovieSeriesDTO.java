package MultimediaServer.dto.movieseries;

import MultimediaServer.models.GenreEntity;
import MultimediaServer.models.MovieSeriesEntity;
import MultimediaServer.models.enums.EMovieSeriesType;

import java.util.List;

// posterPath: nombre del archivo relativo a app.storage.posters (null si no tiene póster).
public record FullMovieSeriesDTO(
        Long id,
        String name,
        String synopsis,
        String posterPath,
        Short releaseYear,
        String classification,
        EMovieSeriesType type,
        List<Long> genreIds
) {
    public FullMovieSeriesDTO(MovieSeriesEntity e) {
        this(e.getId(), e.getName(), e.getSynopsis(), e.getPosterPath(), e.getReleaseYear(),
                e.getClassification(), e.getType(),
                e.getGenres().stream().map(GenreEntity::getId).sorted().toList());
    }
}
