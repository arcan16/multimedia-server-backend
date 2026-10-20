package MultimediaServer.validations.multimedia.update;

import MultimediaServer.dto.multimedia.UpdateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.MultimediaEntity;
import MultimediaServer.models.enums.EMovieSeriesType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Una MOVIE no lleva temporada ni episodio. En una SERIE son opcionales en el PUT (null = sin cambios).
@Component
@Order(1)
public class ValidateUpdateEpisodeData implements IUpdateMultimediaValidation {

    @Override
    public void validate(UpdateMultimediaDTO dto, HttpServletRequest request, MultimediaEntity entity) {
        if (entity.getMovieSeries().getType() == EMovieSeriesType.MOVIE
                && (dto.season() != null || dto.episodeNumber() != null)) {
            throw new MyIntegrityValidationException(
                    "Error, una película no lleva temporada ni episodio", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
