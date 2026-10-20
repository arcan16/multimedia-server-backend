package MultimediaServer.validations.multimedia.create;

import MultimediaServer.dto.multimedia.CreateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.enums.EMovieSeriesType;
import MultimediaServer.repositories.MovieSeriesRepository;
import MultimediaServer.repositories.MultimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Una MOVIE solo tiene un archivo activo: para cambiar el vídeo se usa el PUT.
@Component
public class ValidateSingleMovieFile implements ICreateMultimediaValidation {

    @Autowired
    private MovieSeriesRepository movieSeriesRepository;

    @Autowired
    private MultimediaRepository multimediaRepository;

    @Override
    public void validate(CreateMultimediaDTO dto) {
        movieSeriesRepository.findByIdAndDeletedAtIsNull(dto.movieSeriesId()).ifPresent(movieSeries -> {
            if (movieSeries.getType() == EMovieSeriesType.MOVIE
                    && multimediaRepository.existsByMovieSeries_IdAndDeletedAtIsNull(movieSeries.getId())) {
                throw new MyIntegrityValidationException(
                        "Error, la película ya tiene un vídeo; para reemplazarlo usa la actualización", 400, HttpStatus.BAD_REQUEST);
            }
        });
    }
}
