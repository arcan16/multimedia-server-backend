package MultimediaServer.validations.user.delete;

import MultimediaServer.models.UserEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface IDeleteUserValidation {
    void validate(HttpServletRequest request, UserEntity entity);
}
