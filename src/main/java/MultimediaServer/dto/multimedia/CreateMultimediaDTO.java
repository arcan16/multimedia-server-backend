package MultimediaServer.dto.multimedia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Llega como multipart/form-data (@ModelAttribute); el vídeo va aparte en @RequestPart("video").
// La duración la calcula el servidor con ffprobe. season/episodeNumber: obligatorios en una SERIE, prohibidos en una MOVIE.
public record CreateMultimediaDTO(
        @NotNull(message = "La película o serie es obligatoria")
        Long movieSeriesId,

        @Size(max = 100, message = "El título no puede superar los 100 caracteres")
        String title,

        // SMALLINT UNSIGNED; la temporada 0 se usa para especiales.
        @Min(value = 0, message = "La temporada debe estar entre 0 y 65535")
        @Max(value = 65535, message = "La temporada debe estar entre 0 y 65535")
        Integer season,

        @Min(value = 1, message = "El episodio debe estar entre 1 y 65535")
        @Max(value = 65535, message = "El episodio debe estar entre 1 y 65535")
        Integer episodeNumber
) {
}
