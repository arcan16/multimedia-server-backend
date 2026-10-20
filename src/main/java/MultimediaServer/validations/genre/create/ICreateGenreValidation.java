package MultimediaServer.validations.genre.create;

import MultimediaServer.dto.genre.CreateGenreDTO;

public interface ICreateGenreValidation {
    void validate(CreateGenreDTO dto);
}
