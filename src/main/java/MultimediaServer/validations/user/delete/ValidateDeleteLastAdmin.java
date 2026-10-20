package MultimediaServer.validations.user.delete;

import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Un ADMIN que elimina su cuenta necesita que quede al menos otro ADMIN activo.
@Component
public class ValidateDeleteLastAdmin implements IDeleteUserValidation {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void validate(HttpServletRequest request, UserEntity entity) {
        if (entity.getRol() != ERol.ADMIN) {
            return;
        }
        boolean otherAdminExists = userRepository.findAllByRolAndStatus(ERol.ADMIN, EUserStatus.ACTIVE).stream()
                .anyMatch(admin -> !admin.getId().equals(entity.getId()));
        if (!otherAdminExists) {
            throw new MyIntegrityValidationException("Debe existir al menos un administrador activo", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
