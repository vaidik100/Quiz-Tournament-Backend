package cs.sahil.QuizAPIBackend.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    // Use a secure random key generator for HS512
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Secure 512-bit key

    private final long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    public String generateJwtToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }


    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // Ensure this matches the key used for signing
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // Extract the subject (username)
    }
    public List<String> getRolesFromJwtToken(String token) {
        return (List<String>) Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
    }


    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // Ensure this matches the token signing key
                    .build()
                    .parseClaimsJws(token);
            System.out.println("Valid JWT Token: " + token);
            return true;
        } catch (Exception e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
            return false;
        }
    }


}
