package MultimediaServer.validations.user.update;

import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUpdateUniqueUsername implements IUpdateUserValidation {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void validate(UpdateUserDTO dto, HttpServletRequest request, UserEntity entity) {
        if (dto.username() != null
                && userRepository.existsByUsernameAndStatusNotAndIdNot(dto.username(), EUserStatus.DELETED, entity.getId())) {
            throw new MyIntegrityValidationException("Error, el nombre de usuario ya está en uso", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
