package MultimediaServer.infra.security.services;

import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Solo los usuarios ACTIVE pueden autenticarse; INACTIVE y DELETED se tratan como inexistentes.
        UserEntity userDetails = userRepository.findByUsernameAndStatus(username, EUserStatus.ACTIVE)
                .orElseThrow(()->new UsernameNotFoundException("El usuario "+ username +" no existe"));

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_"+ userDetails.getRol()));

        return new User(userDetails.getUsername(), userDetails.getPassword(),
                true,
                true,
                true,
                true,
                authorities);
    }
}
