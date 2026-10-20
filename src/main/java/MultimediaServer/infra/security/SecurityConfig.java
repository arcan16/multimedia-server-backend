package MultimediaServer.infra.security;

import MultimediaServer.infra.security.filters.JwtAuthenticationFilter;
import MultimediaServer.infra.security.filters.JwtAuthorizationFilter;
import MultimediaServer.infra.security.utils.JwtUtils;
import MultimediaServer.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    private UserRepository userRepository;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                            AuthenticationManager authenticationManager) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils, userRepository);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAuthenticationFilter.setFilterProcessesUrl("/login");

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                // Usa el CorsConfigurationSource de CorsConfig; el filtro CORS responde el preflight antes de autenticar.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth->{
                    // /error publico: si no, cualquier 404/500 se reenvia ahi sin autenticar y acaba en 401/403.
                    auth.requestMatchers("/error").permitAll();
                    // Preflight CORS: el navegador nunca manda el token en un OPTIONS.
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    // Listar y crear usuarios es solo de ADMIN; el resto de reglas de /users depende
                    // del usuario objetivo y se valida en UserService.
                    auth.requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN");
                    // Géneros: cualquier usuario autenticado los consulta; solo ADMIN los gestiona.
                    auth.requestMatchers(HttpMethod.POST, "/genres").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/genres").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/genres/*").hasRole("ADMIN");
                    // Películas y series: igual que los géneros.
                    auth.requestMatchers(HttpMethod.POST, "/movies-series").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/movies-series").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/movies-series/*").hasRole("ADMIN");
                    // Archivos multimedia: igual que el catálogo.
                    auth.requestMatchers(HttpMethod.POST, "/multimedia").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/multimedia").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/multimedia/*").hasRole("ADMIN");
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session->{
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .exceptionHandling(ex->{
                    // Sin token (o con token invalido) → 401 en JSON; por defecto Spring respondería 403 sin cuerpo.
                    ex.authenticationEntryPoint((request, response, authException)->{
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.getWriter().write(
                                "{\"message\":\"Se requiere autenticacion\",\"code\":401,\"status\":401}");
                    });
                    // Autenticado pero sin el rol requerido → 403 en JSON (por defecto, sin cuerpo).
                    ex.accessDeniedHandler((request, response, accessDeniedException)->{
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.getWriter().write(
                                "{\"message\":\"No tienes permisos para realizar esta accion\",\"code\":403,\"status\":403}");
                    });
                })
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity httpSecurity,
                                                PasswordEncoder passwordEncoder) throws Exception {

        AuthenticationManagerBuilder authBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return authBuilder.build();

    }
}
