package MultimediaServer.dto.genre;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Solo se actualizan los campos distintos de null. @Pattern rechaza cadenas en blanco y deja pasar null.
public record UpdateGenreDTO(
        @NotNull(message = "El id es obligatorio")
        Long id,

        @Pattern(regexp = ".*\\S.*", message = "El género no puede estar vacío")
        @Size(max = 50, message = "El género no puede superar los 50 caracteres")
        String genre
) {
}
