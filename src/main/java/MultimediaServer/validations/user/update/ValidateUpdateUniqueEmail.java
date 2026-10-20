package MultimediaServer.validations.user.update;

import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.UserEntity;
import MultimediaServer.repositories.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUpdateUniqueEmail implements IUpdateUserValidation {

    @Autowired
    private PersonRepository personRepository;

    @Override
    public void validate(UpdateUserDTO dto, HttpServletRequest request, UserEntity entity) {
        if (dto.email() != null
                && personRepository.existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(dto.email(), entity.getId())) {
            throw new MyIntegrityValidationException("Error, el email ya está registrado", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
