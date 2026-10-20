package MultimediaServer.dto.movieseries;

import MultimediaServer.models.enums.EMovieSeriesType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Llega como multipart/form-data (@ModelAttribute); el póster va aparte en @RequestPart("poster").
public record CreateMovieSeriesDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "La sinopsis es obligatoria")
        String synopsis,

        // Rango de la columna YEAR de MySQL.
        @NotNull(message = "El año de estreno es obligatorio")
        @Min(value = 1901, message = "El año de estreno debe estar entre 1901 y 2155")
        @Max(value = 2155, message = "El año de estreno debe estar entre 1901 y 2155")
        Short releaseYear,

        @Size(max = 10, message = "La clasificación no puede superar los 10 caracteres")
        String classification,

        @NotNull(message = "El tipo es obligatorio (MOVIE o SERIE)")
        EMovieSeriesType type,

        List<Long> genreIds
) {
}
