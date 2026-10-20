package MultimediaServer.services;

import MultimediaServer.dto.genre.CreateGenreDTO;
import MultimediaServer.dto.genre.FullGenreDTO;
import MultimediaServer.dto.genre.UpdateGenreDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.GenreEntity;
import MultimediaServer.repositories.GenreRepository;
import MultimediaServer.validations.genre.create.ICreateGenreValidation;
import MultimediaServer.validations.genre.delete.IDeleteGenreValidation;
import MultimediaServer.validations.genre.update.IUpdateGenreValidation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// GENRES no tiene columnas de auditoría: sin AUDIT_LOG y con borrado físico.
// Crear, actualizar y borrar es solo de ADMIN (restringido en SecurityConfig).
@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private List<ICreateGenreValidation> createValidations;

    @Autowired
    private List<IUpdateGenreValidation> updateValidations;

    @Autowired
    private List<IDeleteGenreValidation> deleteValidations;

    public List<FullGenreDTO> getAll(Pageable page) {
        return genreRepository.findAll(page).map(FullGenreDTO::new).toList();
    }

    public FullGenreDTO getById(Long id) {
        return new FullGenreDTO(findById(id));
    }

    @Transactional
    public FullGenreDTO create(CreateGenreDTO dto) {
        createValidations.forEach(v -> v.validate(dto));

        GenreEntity genre = new GenreEntity();
        genre.setGenre(dto.genre().trim());

        return new FullGenreDTO(genreRepository.save(genre));
    }

    @Transactional
    public void update(UpdateGenreDTO dto, HttpServletRequest request) {
        GenreEntity genre = findById(dto.id());
        updateValidations.forEach(v -> v.validate(dto, request, genre));

        if (dto.genre() != null) {
            genre.setGenre(dto.genre().trim());
        }
    }

    public void delete(Long id, HttpServletRequest request) {
        GenreEntity genre = findById(id);
        deleteValidations.forEach(v -> v.validate(request, genre));

        genreRepository.delete(genre);
    }

    private GenreEntity findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new MyIntegrityValidationException("El registro indicado no existe", 404, HttpStatus.NOT_FOUND));
    }
}
