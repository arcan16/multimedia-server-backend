package MultimediaServer.validations.multimedia.update;

import MultimediaServer.dto.multimedia.UpdateMultimediaDTO;
import MultimediaServer.models.MultimediaEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface IUpdateMultimediaValidation {
    void validate(UpdateMultimediaDTO dto, HttpServletRequest request, MultimediaEntity entity);
}
