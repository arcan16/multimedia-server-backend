package MultimediaServer.validations.user.update;

import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.models.UserEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface IUpdateUserValidation {
    void validate(UpdateUserDTO dto, HttpServletRequest request, UserEntity entity);
}
