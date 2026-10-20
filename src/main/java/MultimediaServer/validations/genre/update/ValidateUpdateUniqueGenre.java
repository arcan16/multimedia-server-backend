package MultimediaServer.validations.genre.update;

import MultimediaServer.dto.genre.UpdateGenreDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.GenreEntity;
import MultimediaServer.repositories.GenreRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUpdateUniqueGenre implements IUpdateGenreValidation {

    @Autowired
    private GenreRepository genreRepository;

    @Override
    public void validate(UpdateGenreDTO dto, HttpServletRequest request, GenreEntity entity) {
        if (dto.genre() != null
                && genreRepository.existsByGenreIgnoreCaseAndIdNot(dto.genre().trim(), entity.getId())) {
            throw new MyIntegrityValidationException("Error, el género ya existe", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
