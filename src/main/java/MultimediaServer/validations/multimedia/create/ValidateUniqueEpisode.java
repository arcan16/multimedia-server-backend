package MultimediaServer.validations.multimedia.create;

import MultimediaServer.dto.multimedia.CreateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.repositories.MultimediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUniqueEpisode implements ICreateMultimediaValidation {

    @Autowired
    private MultimediaRepository multimediaRepository;

    @Override
    public void validate(CreateMultimediaDTO dto) {
        if (dto.season() == null || dto.episodeNumber() == null) {
            return;
        }
        if (multimediaRepository.existsByMovieSeries_IdAndSeasonAndEpisodeNumberAndDeletedAtIsNull(
                dto.movieSeriesId(), dto.season(), dto.episodeNumber())) {
            throw new MyIntegrityValidationException(
                    "Error, ya existe ese episodio en esa temporada de la serie", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
