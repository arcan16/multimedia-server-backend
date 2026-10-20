package MultimediaServer.services;

import MultimediaServer.dto.multimedia.CreateMultimediaDTO;
import MultimediaServer.dto.multimedia.FullMultimediaDTO;
import MultimediaServer.dto.multimedia.UpdateMultimediaDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.infra.services.AuditLogService;
import MultimediaServer.infra.services.FileStorageService;
import MultimediaServer.infra.services.MediaProbeService;
import MultimediaServer.models.MovieSeriesEntity;
import MultimediaServer.models.MultimediaEntity;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EAuditAction;
import MultimediaServer.repositories.MovieSeriesRepository;
import MultimediaServer.repositories.MultimediaRepository;
import MultimediaServer.validations.multimedia.create.ICreateMultimediaValidation;
import MultimediaServer.validations.multimedia.update.IUpdateMultimediaValidation;
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
import java.util.List;
import java.util.Map;

// Crear, actualizar y borrar es solo de ADMIN (restringido en SecurityConfig).
@Service
public class MultimediaService {

    private static final String TABLE_NAME = "Multimedia";

    // Formatos que los navegadores reproducen en <video>.
    private static final Map<String, String> VIDEO_TYPES = Map.of(
            "video/mp4", "mp4",
            "video/webm", "webm");

    // Jackson 3 (el de Spring Boot 4): serializa java.time sin módulos extra.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.storage.videos}")
    private String videosPath;

    @Autowired
    private MultimediaRepository multimediaRepository;

    @Autowired
    private MovieSeriesRepository movieSeriesRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MediaProbeService mediaProbeService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private List<ICreateMultimediaValidation> createValidations;

    @Autowired
    private List<IUpdateMultimediaValidation> updateValidations;

    // movieSeriesId opcional: filtra los archivos de una película o serie.
    @Transactional(readOnly = true)
    public List<FullMultimediaDTO> getAll(Long movieSeriesId, Pageable page) {
        var result = movieSeriesId == null
                ? multimediaRepository.findAllByDeletedAtIsNull(page)
                : multimediaRepository.findAllByMovieSeries_IdAndDeletedAtIsNull(movieSeriesId, page);
        return result.map(FullMultimediaDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public FullMultimediaDTO getById(Long id) {
        return new FullMultimediaDTO(findActive(id));
    }

    @Transactional(readOnly = true)
    public Resource getVideo(Long id) {
        return fileStorageService.load(videosPath, findActive(id).getMultimediaPath());
    }

    @Transactional
    public FullMultimediaDTO create(CreateMultimediaDTO dto, MultipartFile video, HttpServletRequest request) {
        createValidations.forEach(v -> v.validate(dto));
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        MovieSeriesEntity movieSeries = movieSeriesRepository.findByIdAndDeletedAtIsNull(dto.movieSeriesId()).orElseThrow();

        MultimediaEntity multimedia = new MultimediaEntity();
        multimedia.setMovieSeries(movieSeries);
        multimedia.setTitle(dto.title());
        multimedia.setSeason(dto.season());
        multimedia.setEpisodeNumber(dto.episodeNumber());
        multimedia.setCreatedBy(currentUser);
        storeVideo(multimedia, video);
        multimediaRepository.save(multimedia);

        FullMultimediaDTO created = new FullMultimediaDTO(multimedia);
        auditLogService.create(currentUser, EAuditAction.INSERT, TABLE_NAME, multimedia.getId(), null, toJson(created));
        return created;
    }

    // video opcional: si llega, reemplaza al anterior (que se borra del disco al confirmar la transacción).
    @Transactional
    public void update(UpdateMultimediaDTO dto, MultipartFile video, HttpServletRequest request) {
        MultimediaEntity multimedia = findActive(dto.id());
        updateValidations.forEach(v -> v.validate(dto, request, multimedia));

        String oldValues = toJson(new FullMultimediaDTO(multimedia));

        if (dto.title() != null) {
            multimedia.setTitle(dto.title());
        }
        if (dto.season() != null) {
            multimedia.setSeason(dto.season());
        }
        if (dto.episodeNumber() != null) {
            multimedia.setEpisodeNumber(dto.episodeNumber());
        }
        if (video != null && !video.isEmpty()) {
            String oldVideo = multimedia.getMultimediaPath();
            storeVideo(multimedia, video);
            fileStorageService.deleteAfterCommit(videosPath, oldVideo);
        }

        auditLogService.create(request, EAuditAction.UPDATE, TABLE_NAME, multimedia.getId(),
                oldValues, toJson(new FullMultimediaDTO(multimedia)));
    }

    // Borrado lógico: el vídeo se conserva en disco (el registro sigue existiendo en la BD).
    @Transactional
    public void delete(Long id, HttpServletRequest request) {
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        MultimediaEntity multimedia = findActive(id);

        String oldValues = toJson(new FullMultimediaDTO(multimedia));

        multimedia.setDeletedAt(LocalDateTime.now());
        multimedia.setDeletedBy(currentUser);

        auditLogService.create(currentUser, EAuditAction.DELETE, TABLE_NAME, multimedia.getId(), oldValues, null);
    }

    private MultimediaEntity findActive(Long id) {
        return multimediaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new MyIntegrityValidationException("El registro indicado no existe", 404, HttpStatus.NOT_FOUND));
    }

    // Guarda el archivo y calcula su duración con ffprobe. Si ffprobe falla, la excepción hace rollback
    // y FileStorageService borra el archivo recién guardado.
    private void storeVideo(MultimediaEntity multimedia, MultipartFile video) {
        // Sin límite propio: manda spring.servlet.multipart.max-file-size.
        String fileName = fileStorageService.store(video, videosPath, VIDEO_TYPES, Long.MAX_VALUE);
        multimedia.setMultimediaPath(fileName);
        multimedia.setDurationSeconds(mediaProbeService.getDurationSeconds(fileStorageService.resolve(videosPath, fileName)));
    }

    private String toJson(FullMultimediaDTO dto) {
        return objectMapper.writeValueAsString(dto);
    }
}
