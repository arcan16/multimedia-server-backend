package MultimediaServer.controllers;

import MultimediaServer.dto.user.CreateUserDTO;
import MultimediaServer.dto.user.FullUserDTO;
import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.services.UserService;
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
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAll(@PageableDefault(size = 10) Pageable page) {
        List<FullUserDTO> users = userService.getAll(page);
        if (users.isEmpty()) {
            return ResponseEntity.ok("{\"message\":\"No existen usuarios registrados\"}");
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, HttpServletRequest request) {
        return ResponseEntity.ok(userService.getById(id, request));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserDTO dto, HttpServletRequest request,
                                    UriComponentsBuilder uriComponentsBuilder) {
        FullUserDTO created = userService.create(dto, request);
        URI url = uriComponentsBuilder.path("/users/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(url).body(created);
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody UpdateUserDTO dto, HttpServletRequest request) {
        userService.update(dto, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        userService.delete(id, request);
        return ResponseEntity.ok("{\"message\":\"Registro eliminado correctamente\"}");
    }
}
