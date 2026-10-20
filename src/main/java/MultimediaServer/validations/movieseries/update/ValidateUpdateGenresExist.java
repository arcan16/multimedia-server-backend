package MultimediaServer.validations.movieseries.update;

import MultimediaServer.dto.movieseries.UpdateMovieSeriesDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.MovieSeriesEntity;
import MultimediaServer.repositories.GenreRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ValidateUpdateGenresExist implements IUpdateMovieSeriesValidation {

    @Autowired
    private GenreRepository genreRepository;

    @Override
    public void validate(UpdateMovieSeriesDTO dto, HttpServletRequest request, MovieSeriesEntity entity) {
        if (dto.genreIds() == null || dto.genreIds().isEmpty()) {
            return;
        }
        Set<Long> ids = new HashSet<>(dto.genreIds());
        if (ids.contains(null) || genreRepository.findAllById(ids).size() != ids.size()) {
            throw new MyIntegrityValidationException("Error, uno o más géneros indicados no existen", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
