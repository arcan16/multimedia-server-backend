package MultimediaServer.validations.genre.create;

import MultimediaServer.dto.genre.CreateGenreDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.repositories.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUniqueGenre implements ICreateGenreValidation {

    @Autowired
    private GenreRepository genreRepository;

    @Override
    public void validate(CreateGenreDTO dto) {
        if (genreRepository.existsByGenreIgnoreCase(dto.genre().trim())) {
            throw new MyIntegrityValidationException("Error, el género ya existe", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
