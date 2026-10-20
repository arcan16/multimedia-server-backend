package MultimediaServer.validations.user.update;

import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Un ADMIN que se degrada a USER necesita que quede al menos otro ADMIN activo.
@Component
public class ValidateUpdateLastAdmin implements IUpdateUserValidation {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void validate(UpdateUserDTO dto, HttpServletRequest request, UserEntity entity) {
        if (entity.getRol() != ERol.ADMIN || dto.rol() != ERol.USER) {
            return;
        }
        boolean otherAdminExists = userRepository.findAllByRolAndStatus(ERol.ADMIN, EUserStatus.ACTIVE).stream()
                .anyMatch(admin -> !admin.getId().equals(entity.getId()));
        if (!otherAdminExists) {
            throw new MyIntegrityValidationException("Debe existir al menos un administrador activo", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
