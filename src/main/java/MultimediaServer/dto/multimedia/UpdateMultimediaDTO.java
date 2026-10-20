package MultimediaServer.dto.multimedia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Solo se actualizan los campos distintos de null. No se puede mover a otra película o serie.
public record UpdateMultimediaDTO(
        @NotNull(message = "El id es obligatorio")
        Long id,

        @Size(max = 100, message = "El título no puede superar los 100 caracteres")
        String title,

        @Min(value = 0, message = "La temporada debe estar entre 0 y 65535")
        @Max(value = 65535, message = "La temporada debe estar entre 0 y 65535")
        Integer season,

        @Min(value = 1, message = "El episodio debe estar entre 1 y 65535")
        @Max(value = 65535, message = "El episodio debe estar entre 1 y 65535")
        Integer episodeNumber
) {
}
