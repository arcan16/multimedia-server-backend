package MultimediaServer.validations.user.delete;

import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

// Un USER no elimina ninguna cuenta. Un ADMIN elimina la suya y las de USER, nunca la de otro ADMIN.
@Component
@Order(1)
public class ValidateDeletePermissions implements IDeleteUserValidation {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void validate(HttpServletRequest request, UserEntity entity) {
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);

        if (currentUser.getRol() == ERol.USER) {
            throw new MyIntegrityValidationException("No tienes permisos para eliminar cuentas", 403, HttpStatus.FORBIDDEN);
        }
        if (entity.getRol() == ERol.ADMIN && !currentUser.getId().equals(entity.getId())) {
            throw new MyIntegrityValidationException("No puedes eliminar la cuenta de otro administrador", 403, HttpStatus.FORBIDDEN);
        }
    }
}
