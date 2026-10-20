package MultimediaServer.validations.movieseries.update;

import MultimediaServer.dto.movieseries.UpdateMovieSeriesDTO;
import MultimediaServer.models.MovieSeriesEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface IUpdateMovieSeriesValidation {
    void validate(UpdateMovieSeriesDTO dto, HttpServletRequest request, MovieSeriesEntity entity);
}
