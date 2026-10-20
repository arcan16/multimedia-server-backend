package MultimediaServer.services;

import MultimediaServer.dto.movieseries.CreateMovieSeriesDTO;
import MultimediaServer.dto.movieseries.FullMovieSeriesDTO;
import MultimediaServer.dto.movieseries.UpdateMovieSeriesDTO;
import MultimediaServer.dto.multimedia.FullMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.infra.services.AuditLogService;
import MultimediaServer.infra.services.FileStorageService;
import MultimediaServer.models.GenreEntity;
import MultimediaServer.models.MovieSeriesEntity;
import MultimediaServer.models.MultimediaEntity;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EAuditAction;
import MultimediaServer.repositories.GenreRepository;
import MultimediaServer.repositories.MovieSeriesRepository;
import MultimediaServer.repositories.MultimediaRepository;
import MultimediaServer.validations.movieseries.create.ICreateMovieSeriesValidation;
import MultimediaServer.validations.movieseries.update.IUpdateMovieSeriesValidation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Crear, actualizar y borrar es solo de ADMIN (restringido en SecurityConfig).
@Service
public class MovieSeriesService {

    private static final String TABLE_NAME = "MovieSeries";

    private static final long MAX_POSTER_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> POSTER_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    // Jackson 3 (el de Spring Boot 4): serializa java.time sin módulos extra.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.storage.posters}")
    private String postersPath;

    @Autowired
    private MovieSeriesRepository movieSeriesRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MultimediaRepository multimediaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private List<ICreateMovieSeriesValidation> createValidations;

    @Autowired
    private List<IUpdateMovieSeriesValidation> updateValidations;

    // name opcional: filtra por nombre que contenga el texto, sin distinguir mayúsculas.
    @Transactional(readOnly = true)
    public List<FullMovieSeriesDTO> getAll(String name, Pageable page) {
        var result = name == null || name.isBlank()
                ? movieSeriesRepository.findAllByDeletedAtIsNull(page)
                : movieSeriesRepository.findAllByNameContainingIgnoreCaseAndDeletedAtIsNull(name.trim(), page);
        return result.map(FullMovieSeriesDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public FullMovieSeriesDTO getById(Long id) {
        return new FullMovieSeriesDTO(findActive(id));
    }

    @Transactional(readOnly = true)
    public Resource getPoster(Long id) {
        MovieSeriesEntity movieSeries = findActive(id);
        if (movieSeries.getPosterPath() == null) {
            throw new MyIntegrityValidationException("La película o serie no tiene póster", 404, HttpStatus.NOT_FOUND);
        }
        return fileStorageService.load(postersPath, movieSeries.getPosterPath());
    }

    @Transactional
    public FullMovieSeriesDTO create(CreateMovieSeriesDTO dto, MultipartFile poster, HttpServletRequest request) {
        createValidations.forEach(v -> v.validate(dto));
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);

        MovieSeriesEntity movieSeries = new MovieSeriesEntity();
        movieSeries.setName(dto.name().trim());
        movieSeries.setSynopsis(dto.synopsis().trim());
        movieSeries.setReleaseYear(dto.releaseYear());
        movieSeries.setClassification(dto.classification());
        movieSeries.setType(dto.type());
        movieSeries.setGenres(findGenres(dto.genreIds()));
        movieSeries.setCreatedBy(currentUser);
        if (hasFile(poster)) {
            movieSeries.setPosterPath(fileStorageService.store(poster, postersPath, POSTER_TYPES, MAX_POSTER_BYTES));
        }
        movieSeriesRepository.save(movieSeries);

        FullMovieSeriesDTO created = new FullMovieSeriesDTO(movieSeries);
        auditLogService.create(currentUser, EAuditAction.INSERT, TABLE_NAME, movieSeries.getId(), null, toJson(created));
        return created;
    }

    // poster opcional: si llega, reemplaza al anterior (que se borra del disco al confirmar la transacción).
    @Transactional
    public void update(UpdateMovieSeriesDTO dto, MultipartFile poster, HttpServletRequest request) {
        MovieSeriesEntity movieSeries = findActive(dto.id());
        updateValidations.forEach(v -> v.validate(dto, request, movieSeries));

        String oldValues = toJson(new FullMovieSeriesDTO(movieSeries));

        if (dto.name() != null) {
            movieSeries.setName(dto.name().trim());
        }
        if (dto.synopsis() != null) {
            movieSeries.setSynopsis(dto.synopsis().trim());
        }
        if (dto.releaseYear() != null) {
            movieSeries.setReleaseYear(dto.releaseYear());
        }
        if (dto.classification() != null) {
            movieSeries.setClassification(dto.classification());
        }
        if (dto.type() != null) {
            movieSeries.setType(dto.type());
        }
        if (dto.genreIds() != null) {
            movieSeries.setGenres(findGenres(dto.genreIds()));
        }
        if (hasFile(poster)) {
            String oldPoster = movieSeries.getPosterPath();
            movieSeries.setPosterPath(fileStorageService.store(poster, postersPath, POSTER_TYPES, MAX_POSTER_BYTES));
            fileStorageService.deleteAfterCommit(postersPath, oldPoster);
        }

        auditLogService.create(request, EAuditAction.UPDATE, TABLE_NAME, movieSeries.getId(),
                oldValues, toJson(new FullMovieSeriesDTO(movieSeries)));
    }

    // Borrado lógico en cascada: también marca como borrados (y audita) sus registros de MULTIMEDIA activos.
    // El póster se conserva en disco (el registro sigue existiendo en la BD).
    @Transactional
    public void delete(Long id, HttpServletRequest request) {
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        MovieSeriesEntity movieSeries = findActive(id);

        String oldValues = toJson(new FullMovieSeriesDTO(movieSeries));
        LocalDateTime now = LocalDateTime.now();

        movieSeries.setDeletedAt(now);
        movieSeries.setDeletedBy(currentUser);

        for (MultimediaEntity multimedia : multimediaRepository.findAllByMovieSeries_IdAndDeletedAtIsNull(id)) {
            String oldMultimedia = toJson(new FullMultimediaDTO(multimedia));
            multimedia.setDeletedAt(now);
            multimedia.setDeletedBy(currentUser);
            auditLogService.create(currentUser, EAuditAction.DELETE, "Multimedia", multimedia.getId(), oldMultimedia, null);
        }

        auditLogService.create(currentUser, EAuditAction.DELETE, TABLE_NAME, movieSeries.getId(), oldValues, null);
    }

    private MovieSeriesEntity findActive(Long id) {
        return movieSeriesRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new MyIntegrityValidationException("El registro indicado no existe", 404, HttpStatus.NOT_FOUND));
    }

    // Las validaciones ya comprobaron que todos los ids existen.
    private Set<GenreEntity> findGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(genreRepository.findAllById(genreIds));
    }

    // Un campo de archivo enviado vacío en el formulario cuenta como "sin archivo".
    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String toJson(Object dto) {
        return objectMapper.writeValueAsString(dto);
    }
}
