package MultimediaServer.dto.movieseries;

import MultimediaServer.models.enums.EMovieSeriesType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

// Solo se actualizan los campos distintos de null. genreIds, si llega, reemplaza todos los géneros.
// @Pattern rechaza cadenas en blanco y deja pasar null.
public record UpdateMovieSeriesDTO(
        @NotNull(message = "El id es obligatorio")
        Long id,

        @Pattern(regexp = "(?s).*\\S.*", message = "El nombre no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Pattern(regexp = "(?s).*\\S.*", message = "La sinopsis no puede estar vacía")
        String synopsis,

        @Min(value = 1901, message = "El año de estreno debe estar entre 1901 y 2155")
        @Max(value = 2155, message = "El año de estreno debe estar entre 1901 y 2155")
        Short releaseYear,

        @Size(max = 10, message = "La clasificación no puede superar los 10 caracteres")
        String classification,

        EMovieSeriesType type,

        List<Long> genreIds
) {
}
