package MultimediaServer.controllers;

import MultimediaServer.dto.movieseries.CreateMovieSeriesDTO;
import MultimediaServer.dto.movieseries.FullMovieSeriesDTO;
import MultimediaServer.dto.movieseries.UpdateMovieSeriesDTO;
import MultimediaServer.models.enums.EMovieSeriesType;
import MultimediaServer.services.MovieSeriesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/movies-series")
public class MovieSeriesController {

    @Autowired
    private MovieSeriesService movieSeriesService;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) EMovieSeriesType type,
                                    @RequestParam(required = false) Long genreId,
                                    @PageableDefault(size = 10) Pageable page) {
        List<FullMovieSeriesDTO> moviesSeries = movieSeriesService.getAll(name, type, genreId, page);
        if (moviesSeries.isEmpty()) {
            return ResponseEntity.ok("{\"message\":\"No existen películas o series registradas\"}");
        }
        return ResponseEntity.ok(moviesSeries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(movieSeriesService.getById(id));
    }

    // Content-Type según la extensión guardada (jpg, png o webp).
    @GetMapping("/{id}/poster")
    public ResponseEntity<?> getPoster(@PathVariable Long id) {
        Resource poster = movieSeriesService.getPoster(id);
        MediaType mediaType = MediaTypeFactory.getMediaType(poster).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(mediaType).body(poster);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(@Valid @ModelAttribute CreateMovieSeriesDTO dto,
                                    @RequestPart(value = "poster", required = false) MultipartFile poster,
                                    HttpServletRequest request, UriComponentsBuilder uriComponentsBuilder) {
        FullMovieSeriesDTO created = movieSeriesService.create(dto, poster, request);
        URI url = uriComponentsBuilder.path("/movies-series/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(url).body(created);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(@Valid @ModelAttribute UpdateMovieSeriesDTO dto,
                                    @RequestPart(value = "poster", required = false) MultipartFile poster,
                                    HttpServletRequest request) {
        movieSeriesService.update(dto, poster, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        movieSeriesService.delete(id, request);
        return ResponseEntity.ok("{\"message\":\"Registro eliminado correctamente\"}");
    }
}
