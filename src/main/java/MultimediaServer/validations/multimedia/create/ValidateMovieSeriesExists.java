package MultimediaServer.validations.multimedia.create;

import MultimediaServer.dto.multimedia.CreateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.repositories.MovieSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Va primero: el resto de validaciones depende del tipo (MOVIE/SERIE) de la película o serie.
@Component
@Order(1)
public class ValidateMovieSeriesExists implements ICreateMultimediaValidation {

    @Autowired
    private MovieSeriesRepository movieSeriesRepository;

    @Override
    public void validate(CreateMultimediaDTO dto) {
        if (movieSeriesRepository.findByIdAndDeletedAtIsNull(dto.movieSeriesId()).isEmpty()) {
            throw new MyIntegrityValidationException("Error, la película o serie indicada no existe", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
