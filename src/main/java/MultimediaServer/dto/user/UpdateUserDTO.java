package MultimediaServer.dto.user;

import MultimediaServer.models.enums.ERol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Solo se actualizan los campos distintos de null. @Pattern rechaza cadenas en blanco y deja pasar null.
public record UpdateUserDTO(
        @NotNull(message = "El id es obligatorio")
        Long id,

        @Pattern(regexp = ".*\\S.*", message = "El nombre de usuario no puede estar vacío")
        @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
        String username,

        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        ERol rol,

        @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String name,

        @Pattern(regexp = ".*\\S.*", message = "El apellido no puede estar vacío")
        @Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
        String lastname,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email
) {
}
