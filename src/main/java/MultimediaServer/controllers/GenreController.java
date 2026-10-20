package MultimediaServer.controllers;

import MultimediaServer.dto.genre.CreateGenreDTO;
import MultimediaServer.dto.genre.FullGenreDTO;
import MultimediaServer.dto.genre.UpdateGenreDTO;
import MultimediaServer.services.GenreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @GetMapping
    public ResponseEntity<?> getAll(@PageableDefault(size = 10) Pageable page) {
        List<FullGenreDTO> genres = genreService.getAll(page);
        if (genres.isEmpty()) {
            return ResponseEntity.ok("{\"message\":\"No existen géneros registrados\"}");
        }
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateGenreDTO dto, UriComponentsBuilder uriComponentsBuilder) {
        FullGenreDTO created = genreService.create(dto);
        URI url = uriComponentsBuilder.path("/genres/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(url).body(created);
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody UpdateGenreDTO dto, HttpServletRequest request) {
        genreService.update(dto, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        genreService.delete(id, request);
        return ResponseEntity.ok("{\"message\":\"Registro eliminado correctamente\"}");
    }
}
