package MultimediaServer.validations.multimedia.update;

import MultimediaServer.dto.multimedia.UpdateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.MultimediaEntity;
import MultimediaServer.repositories.MultimediaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Comprueba la combinación resultante: puede cambiar solo la temporada, solo el episodio o ambos.
@Component
public class ValidateUpdateUniqueEpisode implements IUpdateMultimediaValidation {

    @Autowired
    private MultimediaRepository multimediaRepository;

    @Override
    public void validate(UpdateMultimediaDTO dto, HttpServletRequest request, MultimediaEntity entity) {
        if (dto.season() == null && dto.episodeNumber() == null) {
            return;
        }
        Integer season = dto.season() != null ? dto.season() : entity.getSeason();
        Integer episodeNumber = dto.episodeNumber() != null ? dto.episodeNumber() : entity.getEpisodeNumber();

        if (multimediaRepository.existsByMovieSeries_IdAndSeasonAndEpisodeNumberAndDeletedAtIsNullAndIdNot(
                entity.getMovieSeries().getId(), season, episodeNumber, entity.getId())) {
            throw new MyIntegrityValidationException(
                    "Error, ya existe ese episodio en esa temporada de la serie", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
