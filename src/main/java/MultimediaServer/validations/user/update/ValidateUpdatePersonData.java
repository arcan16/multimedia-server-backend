package MultimediaServer.validations.user.update;

import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Si el usuario aún no tiene persona (p. ej. el admin inicial), para crearla hacen falta los tres campos.
@Component
public class ValidateUpdatePersonData implements IUpdateUserValidation {

    @Override
    public void validate(UpdateUserDTO dto, HttpServletRequest request, UserEntity entity) {
        boolean hasPerson = entity.getPerson() != null && entity.getPerson().getDeletedAt() == null;
        boolean anyPersonField = dto.name() != null || dto.lastname() != null || dto.email() != null;
        boolean allPersonFields = dto.name() != null && dto.lastname() != null && dto.email() != null;

        if (!hasPerson && anyPersonField && !allPersonFields) {
            throw new MyIntegrityValidationException(
                    "Este usuario no tiene datos personales: envía nombre, apellido y email juntos", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
