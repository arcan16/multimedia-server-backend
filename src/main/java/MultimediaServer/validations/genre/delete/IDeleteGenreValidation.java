package MultimediaServer.validations.genre.delete;

import MultimediaServer.models.GenreEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface IDeleteGenreValidation {
    void validate(HttpServletRequest request, GenreEntity entity);
}
