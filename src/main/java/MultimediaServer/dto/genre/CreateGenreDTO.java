package MultimediaServer.dto.genre;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGenreDTO(
        @NotBlank(message = "El género es obligatorio")
        @Size(max = 50, message = "El género no puede superar los 50 caracteres")
        String genre
) {
}
