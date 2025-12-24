package uwu.connectra.connectra_backend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class JwtService {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpirationDays;

    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes)))
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(refreshTokenExpirationDays)))
                .signWith(key())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return isTokenSignatureValid(token) && isTokenNotExpired(token) && isTokenEmailValid(token, userDetails);
    }

    // Check if the token has expired
    private boolean isTokenNotExpired(String token) {
        return !extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Validate the token's signature
    private boolean isTokenSignatureValid(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Check if the token is valid for the given user details
    private boolean isTokenEmailValid(String token, UserDetails userDetails) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(userDetails.getUsername()));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        return claimsResolver.apply(claims);
    }
}