package MultimediaServer.validations.genre.delete;

import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.GenreEntity;
import MultimediaServer.repositories.MovieSeriesRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// GENRES se borra físicamente: la FK de MOVIES_SERIES_GENRES lo impediría si está asociado (incluso a un registro borrado).
@Component
public class ValidateGenreNotInUse implements IDeleteGenreValidation {

    @Autowired
    private MovieSeriesRepository movieSeriesRepository;

    @Override
    public void validate(HttpServletRequest request, GenreEntity entity) {
        if (movieSeriesRepository.existsByGenres_Id(entity.getId())) {
            throw new MyIntegrityValidationException(
                    "Error, el género está asociado a películas o series y no se puede eliminar", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
