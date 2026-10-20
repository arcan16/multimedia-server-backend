package MultimediaServer.validations.movieseries.create;

import MultimediaServer.dto.movieseries.CreateMovieSeriesDTO;

public interface ICreateMovieSeriesValidation {
    void validate(CreateMovieSeriesDTO dto);
}
