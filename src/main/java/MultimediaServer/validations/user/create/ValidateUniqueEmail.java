package MultimediaServer.validations.user.create;

import MultimediaServer.dto.user.CreateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ValidateUniqueEmail implements ICreateUserValidation {

    @Autowired
    private PersonRepository personRepository;

    @Override
    public void validate(CreateUserDTO dto) {
        if (personRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(dto.email())) {
            throw new MyIntegrityValidationException("Error, el email ya está registrado", 400, HttpStatus.BAD_REQUEST);
        }
    }
}
