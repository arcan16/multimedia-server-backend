package MultimediaServer.controllers;

import MultimediaServer.dto.multimedia.CreateMultimediaDTO;
import MultimediaServer.dto.multimedia.FullMultimediaDTO;
import MultimediaServer.dto.multimedia.UpdateMultimediaDTO;
import MultimediaServer.services.MultimediaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/multimedia")
public class MultimediaController {

    @Autowired
    private MultimediaService multimediaService;

    // Ordenado por temporada y episodio para que los episodios de una serie salgan en orden.
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Long movieSeriesId,
                                    @PageableDefault(size = 10, sort = {"season", "episodeNumber"}) Pageable page) {
        List<FullMultimediaDTO> multimedia = multimediaService.getAll(movieSeriesId, page);
        if (multimedia.isEmpty()) {
            return ResponseEntity.ok("{\"message\":\"No existen archivos multimedia registrados\"}");
        }
        return ResponseEntity.ok(multimedia);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(multimediaService.getById(id));
    }

    // Streaming: con un Resource como cuerpo, Spring atiende las cabeceras Range y responde 206 por tramos,
    // que es lo que pide <video> para empezar a reproducir y para saltar a otro punto.
    // Acepta el token como ?token= (ver JwtAuthorizationFilter) porque <video src> no envía cabeceras.
    @GetMapping("/{id}/video")
    public ResponseEntity<?> getVideo(@PathVariable Long id) {
        Resource video = multimediaService.getVideo(id);
        MediaType mediaType = MediaTypeFactory.getMediaType(video).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(mediaType).body(video);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@Valid @ModelAttribute CreateMultimediaDTO dto,
                                    @RequestPart("video") MultipartFile video,
                                    HttpServletRequest request, UriComponentsBuilder uriComponentsBuilder) {
        FullMultimediaDTO created = multimediaService.create(dto, video, request);
        URI url = uriComponentsBuilder.path("/multimedia/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(url).body(created);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@Valid @ModelAttribute UpdateMultimediaDTO dto,
                                    @RequestPart(value = "video", required = false) MultipartFile video,
                                    HttpServletRequest request) {
        multimediaService.update(dto, video, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        multimediaService.delete(id, request);
        return ResponseEntity.ok("{\"message\":\"Registro eliminado correctamente\"}");
    }
}
