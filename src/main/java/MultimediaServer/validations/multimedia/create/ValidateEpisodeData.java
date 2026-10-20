package MultimediaServer.validations.multimedia.create;

import MultimediaServer.dto.multimedia.CreateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.enums.EMovieSeriesType;
import MultimediaServer.repositories.MovieSeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Una MOVIE no lleva temporada ni episodio; una SERIE necesita ambos.
@Component
@Order(2)
public class ValidateEpisodeData implements ICreateMultimediaValidation {

    @Autowired
    private MovieSeriesRepository movieSeriesRepository;

    @Override
    public void validate(CreateMultimediaDTO dto) {
        movieSeriesRepository.findByIdAndDeletedAtIsNull(dto.movieSeriesId()).ifPresent(movieSeries -> {
            boolean hasSeason = dto.season() != null;
            boolean hasEpisode = dto.episodeNumber() != null;

            if (movieSeries.getType() == EMovieSeriesType.MOVIE && (hasSeason || hasEpisode)) {
                throw new MyIntegrityValidationException(
                        "Error, una película no lleva temporada ni episodio", 400, HttpStatus.BAD_REQUEST);
            }
            if (movieSeries.getType() == EMovieSeriesType.SERIE && (!hasSeason || !hasEpisode)) {
                throw new MyIntegrityValidationException(
                        "Error, un episodio de una serie necesita temporada y número de episodio", 400, HttpStatus.BAD_REQUEST);
            }
        });
    }
}
