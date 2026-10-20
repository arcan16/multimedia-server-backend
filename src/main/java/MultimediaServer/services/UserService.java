package MultimediaServer.services;

import MultimediaServer.dto.user.CreateUserDTO;
import MultimediaServer.dto.user.FullUserDTO;
import MultimediaServer.dto.user.UpdateUserDTO;
import MultimediaServer.infra.errors.MyIntegrityValidationException;
import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.infra.services.AuditLogService;
import MultimediaServer.models.PersonEntity;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EAuditAction;
import MultimediaServer.models.enums.ERol;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.PersonRepository;
import MultimediaServer.repositories.UserRepository;
import MultimediaServer.validations.user.create.ICreateUserValidation;
import MultimediaServer.validations.user.delete.IDeleteUserValidation;
import MultimediaServer.validations.user.update.IUpdateUserValidation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

// La persona (PERSONS) se gestiona dentro del flujo del usuario: comparten PK y se crean y borran juntos.
@Service
public class UserService {

    private static final String TABLE_NAME = "User";

    // Jackson 3 (el de Spring Boot 4): serializa java.time sin módulos extra.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private List<ICreateUserValidation> createValidations;

    @Autowired
    private List<IUpdateUserValidation> updateValidations;

    @Autowired
    private List<IDeleteUserValidation> deleteValidations;

    // Solo ADMIN (restringido en SecurityConfig).
    @Transactional(readOnly = true)
    public List<FullUserDTO> getAll(Pageable page) {
        return userRepository.findAllByStatusNot(EUserStatus.DELETED, page).map(FullUserDTO::new).toList();
    }

    // Un USER solo puede consultar su propia cuenta. Se comprueba antes de buscar para no revelar qué ids existen.
    @Transactional(readOnly = true)
    public FullUserDTO getById(Long id, HttpServletRequest request) {
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        if (currentUser.getRol() == ERol.USER && !currentUser.getId().equals(id)) {
            throw new MyIntegrityValidationException("Solo puedes consultar tu propia cuenta", 403, HttpStatus.FORBIDDEN);
        }
        return new FullUserDTO(findActive(id));
    }

    @Transactional(readOnly = true)
    public FullUserDTO getMe(HttpServletRequest request) {
        return new FullUserDTO(jwtUtils.getUserFromRequest(request));
    }

    // Solo ADMIN (restringido en SecurityConfig). Puede crear cuentas ADMIN y USER.
    @Transactional
    public FullUserDTO create(CreateUserDTO dto, HttpServletRequest request) {
        createValidations.forEach(v -> v.validate(dto));
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);

        UserEntity user = new UserEntity();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRol(dto.rol() != null ? dto.rol() : ERol.USER);
        user.setCreatedBy(currentUser.getId());
        userRepository.save(user);

        PersonEntity person = new PersonEntity();
        person.setUser(user);
        person.setName(dto.name());
        person.setLastname(dto.lastname());
        person.setEmail(dto.email());
        person.setCreatedBy(currentUser);
        personRepository.save(person);
        user.setPerson(person);

        FullUserDTO created = new FullUserDTO(user);
        auditLogService.create(currentUser, EAuditAction.INSERT, TABLE_NAME, user.getId(), null, toJson(created));
        return created;
    }

    @Transactional
    public void update(UpdateUserDTO dto, HttpServletRequest request) {
        // Se resuelve antes de modificar nada: si el usuario se cambia su propio username,
        // el token (que lleva el username antiguo) ya no encontraría al usuario.
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        UserEntity user = findActive(dto.id());
        updateValidations.forEach(v -> v.validate(dto, request, user));

        String oldValues = toJson(new FullUserDTO(user));

        if (dto.username() != null) {
            user.setUsername(dto.username());
        }
        if (dto.password() != null) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }
        if (dto.rol() != null) {
            user.setRol(dto.rol());
        }
        updatePerson(user, dto, currentUser);

        auditLogService.create(currentUser, EAuditAction.UPDATE, TABLE_NAME, user.getId(),
                oldValues, toJson(new FullUserDTO(user)));
    }

    // Borrado lógico: el usuario pasa a DELETED (libera su username) y su persona se marca como borrada (libera su email).
    @Transactional
    public void delete(Long id, HttpServletRequest request) {
        UserEntity currentUser = jwtUtils.getUserFromRequest(request);
        UserEntity user = findActive(id);
        deleteValidations.forEach(v -> v.validate(request, user));

        String oldValues = toJson(new FullUserDTO(user));
        LocalDateTime now = LocalDateTime.now();

        user.setStatus(EUserStatus.DELETED);
        user.setDeletedAt(now);
        user.setDeletedBy(currentUser.getId());

        PersonEntity person = user.getPerson();
        if (person != null && person.getDeletedAt() == null) {
            person.setDeletedAt(now);
            person.setDeletedBy(currentUser);
        }

        auditLogService.create(currentUser, EAuditAction.DELETE, TABLE_NAME, user.getId(), oldValues, null);
    }

    private UserEntity findActive(Long id) {
        return userRepository.findByIdAndStatusNot(id, EUserStatus.DELETED)
                .orElseThrow(() -> new MyIntegrityValidationException("El registro indicado no existe", 404, HttpStatus.NOT_FOUND));
    }

    // Si el usuario no tiene persona, ValidateUpdatePersonData ya garantizó que vienen los tres campos.
    private void updatePerson(UserEntity user, UpdateUserDTO dto, UserEntity currentUser) {
        if (dto.name() == null && dto.lastname() == null && dto.email() == null) {
            return;
        }
        PersonEntity person = user.getPerson();
        if (person == null) {
            person = new PersonEntity();
            person.setUser(user);
            person.setCreatedBy(currentUser);
            user.setPerson(person);
        }
        if (dto.name() != null) {
            person.setName(dto.name());
        }
        if (dto.lastname() != null) {
            person.setLastname(dto.lastname());
        }
        if (dto.email() != null) {
            person.setEmail(dto.email());
        }
        personRepository.save(person);
    }

    private String toJson(FullUserDTO dto) {
        return objectMapper.writeValueAsString(dto);
    }
}
