import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class InsertTestUser {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java InsertTestUser <jdbcUrl> <dbUser> <dbPassword> [username] [password] [role] [shelterId]");
            System.exit(2);
        }
        String url = args[0];
        String dbUser = args[1];
        String dbPass = args[2];
        String username = args.length >= 4 ? args[3] : "admin_test";
        String password = args.length >= 5 ? args[4] : "Admin@123";
        String role = args.length >= 6 ? args[5] : "Admin";
        String shelterId = args.length >= 7 ? args[6] : null;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);

        try (Connection c = DriverManager.getConnection(url, dbUser, dbPass)) {
            String sql = "INSERT INTO users (username, password_hash, role" + (shelterId != null ? ", shelter_id" : "") + ") VALUES (? , ?, ?" + (shelterId != null ? ", ?" : "") + ")";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, hash);
                ps.setString(3, role);
                if (shelterId != null) ps.setInt(4, Integer.parseInt(shelterId));
                ps.executeUpdate();
            }
        }
        System.out.println("Inserted user: " + username + " with role " + role);
    }
}
