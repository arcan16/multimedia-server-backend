package MultimediaServer.validations.user.update;

import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Un USER solo actualiza su cuenta y no cambia su rol. Un ADMIN actualiza la suya y las de USER, nunca la de otro ADMIN.
// Va primero para no revelar datos (usernames o emails en uso) a quien no puede editar la cuenta.
@Component
@Order(1)
public class ValidateUpdatePermissions implements IUpdateUserValidation {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void validate(UpdateUserDTO dto, HttpServletRequest request, UserEntity entity) {
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        boolean isOwnAccount = currentUser.getId().equals(entity.getId());

        if (currentUser.getRol() == ERol.USER) {
            if (!isOwnAccount) {
                throw new MyIntegrityValidationException("Solo puedes actualizar tu propia cuenta", 403, HttpStatus.FORBIDDEN);
            }
            if (dto.rol() != null && dto.rol() != ERol.USER) {
                throw new MyIntegrityValidationException("No puedes cambiar tu rol", 403, HttpStatus.FORBIDDEN);
            }
            return;
        }

        if (entity.getRol() == ERol.ADMIN && !isOwnAccount) {
            throw new MyIntegrityValidationException("No puedes actualizar la cuenta de otro administrador", 403, HttpStatus.FORBIDDEN);
        }
    }
}
