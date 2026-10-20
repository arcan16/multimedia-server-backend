package MultimediaServer.dto.user;

import MultimediaServer.models.PersonEntity;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import MultimediaServer.models.enums.EUserStatus;

// Nunca expone la contraseña. name/lastname/email son null si el usuario no tiene persona (p. ej. el admin inicial).
public record FullUserDTO(
        Long id,
        String username,
        ERol rol,
        EUserStatus status,
        String name,
        String lastname,
        String email
) {
    public FullUserDTO(UserEntity e) {
        this(e, activePerson(e));
    }

    private FullUserDTO(UserEntity e, PersonEntity p) {
        this(e.getId(), e.getUsername(), e.getRol(), e.getStatus(),
                p != null ? p.getName() : null,
                p != null ? p.getLastname() : null,
                p != null ? p.getEmail() : null);
    }

    private static PersonEntity activePerson(UserEntity e) {
        PersonEntity p = e.getPerson();
        return p != null && p.getDeletedAt() == null ? p : null;
    }
}
