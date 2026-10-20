package MultimediaServer.infra.security.filters;

import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private JwtUtils jwtUtils;
    private UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository){
        this.jwtUtils=jwtUtils;
        this.userRepository=userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UserEntity userEntity = null;
        String username = "";
        String password = "";

        try {
            userEntity = new ObjectMapper().readValue(request.getInputStream(), UserEntity.class);
            username = userEntity.getUsername();
            password = userEntity.getPassword();

        } catch (IOException e) {
            // Body ausente o JSON mal formado: es un fallo de login (401), no un error del servidor.
            throw new AuthenticationServiceException("El cuerpo del login no es un JSON valido", e);
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getPrincipal();
        String token = jwtUtils.generateAccessToken(user.getUsername());
        // El cliente necesita id y rol para saber qué puede hacer sin otra petición.
        UserEntity userEntity = userRepository.findByUsernameAndStatus(user.getUsername(), EUserStatus.ACTIVE)
                .orElseThrow(() -> new AuthenticationServiceException("El usuario autenticado ya no existe"));

        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("Authentication", token);
        httpResponse.put("message","Autenticacion exitosa!!!");
        httpResponse.put("username", user.getUsername());
        httpResponse.put("id", userEntity.getId());
        httpResponse.put("rol", userEntity.getRol().name());

        // No se llama a super.successfulAuthentication: es stateless y su success handler intentaría redirigir.
        writeJson(response, HttpStatus.OK, httpResponse);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("message", "Usuario o contraseña incorrectos");
        httpResponse.put("code", HttpStatus.UNAUTHORIZED.value());
        httpResponse.put("status", HttpStatus.UNAUTHORIZED.value());

        writeJson(response, HttpStatus.UNAUTHORIZED, httpResponse);
    }

    // Cabeceras antes de escribir: una vez escrito el body ya no se pueden cambiar.
    private void writeJson(HttpServletResponse response, HttpStatus status, Map<String, Object> body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.getWriter().flush();
    }
}
