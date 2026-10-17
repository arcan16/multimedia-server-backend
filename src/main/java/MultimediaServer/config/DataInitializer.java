package MultimediaServer.config;

import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.ERol;
import MultimediaServer.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Crea el primer ADMIN cuando USERS esta vacia: sin un usuario inicial nadie podria loguearse.
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:admin}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if(userRepository.count() == 0) {
            UserEntity admin = new UserEntity();

            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRol(ERol.ADMIN);

            userRepository.save(admin);
        }
    }
}
