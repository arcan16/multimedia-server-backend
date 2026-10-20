package MultimediaServer.validations.movieseries.update;

import MultimediaServer.dto.movieseries.UpdateMovieSeriesDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.MovieSeriesEntity;
import MultimediaServer.repositories.MultimediaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Una MOVIE no lleva season/episode_number y una SERIE sí: cambiar el tipo con archivos activos los dejaría incoherentes.
@Component
public class ValidateUpdateTypeChange implements IUpdateMovieSeriesValidation {

    @Autowired
    private MultimediaRepository multimediaRepository;

    @Override
    public void validate(UpdateMovieSeriesDTO dto, HttpServletRequest request, MovieSeriesEntity entity) {
        if (dto.type() != null && dto.type() != entity.getType()
                && multimediaRepository.existsByMovieSeries_IdAndDeletedAtIsNull(entity.getId())) {
            throw new MyIntegrityValidationException(
                    "Error, no se puede cambiar el tipo mientras tenga archivos multimedia asociados", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
