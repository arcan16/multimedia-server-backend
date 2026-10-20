package MultimediaServer.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Lo usa el CorsFilter de Spring Security (http.cors() en SecurityConfig), que va antes de la autenticación:
// así el preflight OPTIONS se responde sin token y las respuestas 401/403 también llevan las cabeceras CORS.
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Cualquier origen y sin credenciales: el token viaja en la cabecera Authorization, no en cookies.
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowCredentials(false);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Range"));
        // Location: URL del recurso creado en los POST. Content-Range/Accept-Ranges: streaming de vídeo.
        config.setExposedHeaders(List.of("Location", "Content-Range", "Accept-Ranges", "Content-Disposition"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
