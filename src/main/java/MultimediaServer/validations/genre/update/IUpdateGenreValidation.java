package MultimediaServer.validations.genre.update;

import MultimediaServer.dto.genre.UpdateGenreDTO;
import MultimediaServer.models.GenreEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface IUpdateGenreValidation {
    void validate(UpdateGenreDTO dto, HttpServletRequest request, GenreEntity entity);
}
