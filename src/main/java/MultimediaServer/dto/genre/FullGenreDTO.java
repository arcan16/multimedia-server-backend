package MultimediaServer.dto.genre;

import MultimediaServer.models.GenreEntity;

public record FullGenreDTO(
        Long id,
        String genre
) {
    public FullGenreDTO(GenreEntity e) {
        this(e.getId(), e.getGenre());
    }
}
