import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class GenerateJwt {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: GenerateJwt <username>");
            System.exit(2);
        }
        String username = args[0];
        String secret = System.getenv().getOrDefault("JWT_SECRET", "change-me-to-a-long-random-secret-key");
        long expMs = 86400000L;
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder()
                .setSubject(username)
                .claim("userId", 9999)
                .claim("role", "Admin")
                .claim("shelterId", null)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        System.out.println(jwt);
    }
}
