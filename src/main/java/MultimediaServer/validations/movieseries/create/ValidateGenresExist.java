package MultimediaServer.validations.movieseries.create;

import MultimediaServer.dto.movieseries.CreateMovieSeriesDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ValidateGenresExist implements ICreateMovieSeriesValidation {

    @Autowired
    private GenreRepository genreRepository;

    @Override
    public void validate(CreateMovieSeriesDTO dto) {
        if (dto.genreIds() == null || dto.genreIds().isEmpty()) {
            return;
        }
        Set<Long> ids = new HashSet<>(dto.genreIds());
        if (ids.contains(null) || genreRepository.findAllById(ids).size() != ids.size()) {
            throw new MyIntegrityValidationException("Error, uno o más géneros indicados no existen", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
