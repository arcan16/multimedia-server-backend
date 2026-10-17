package MultimediaServer.infra.security.utils;

import MultimediaServer.models.UserEntity;
import MultimediaServer.models.enums.EUserStatus;
import MultimediaServer.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration.key}")
    private String expirationKey;

    @Autowired
    private UserRepository userRepository;

    public String generateAccessToken(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Long.parseLong(expirationKey)))
                .signWith(getSignatureKey())
                .compact();
    }

    // JJWT elige HS256/384/512 según la longitud de la clave decodificada (mínimo 32 bytes).
    public SecretKey getSignatureKey(){
        byte [] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    public boolean isValidToken(String token){
        try {
            Jwts.parser().verifyWith(getSignatureKey()).build().parseSignedClaims(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String getSubject(String token){
        return getClaims(token, Claims::getSubject);
    }

    public <T> T getClaims(String token, Function<Claims, T> claimsTFunction){
        Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    public Claims extractAllClaims(String token){
        Jws<Claims> jwsClaims = Jwts.parser().verifyWith(getSignatureKey()).build().parseSignedClaims(token);
        return jwsClaims.getPayload();
    }

    // Filtra por ACTIVE: un username borrado puede reutilizarse y habría varias filas con el mismo nombre.
    public UserEntity getUserFromToken(String stringToken) {
        String username = getSubject(stringToken);
        return userRepository.findByUsernameAndStatus(username, EUserStatus.ACTIVE)
                .orElseThrow(()-> new UsernameNotFoundException("Usuario de token invalido"));
    }
}
