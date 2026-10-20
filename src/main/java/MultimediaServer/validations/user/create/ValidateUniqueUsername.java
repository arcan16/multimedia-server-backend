package MultimediaServer.validations.user.create;

import MultimediaServer.dto.user.CreateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUniqueUsername implements ICreateUserValidation {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void validate(CreateUserDTO dto) {
        if (userRepository.existsByUsernameAndStatusNot(dto.username(), EUserStatus.DELETED)) {
            throw new MyIntegrityValidationException("Error, el nombre de usuario ya está en uso", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
