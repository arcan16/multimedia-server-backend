package MultimediaServer.validations.user.create;

import MultimediaServer.dto.user.CreateUserDTO;

public interface ICreateUserValidation {
    void validate(CreateUserDTO dto);
}
