package kz.amixady.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // нормализован конструктором EmailAddress , в базу — через EmailAddressConverter
    @Column(nullable = false, unique = true, length = 254)
    private EmailAddress email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Account register(EmailAddress email, String passwordHash, Instant now) {
        Account account = new Account();
        account.email = email;
        account.passwordHash = passwordHash;
        account.emailVerified = false;
        account.createdAt = now;
        return account;
    }

    public void confirmEmail() {
        this.emailVerified = true;
    }

    public boolean passwordMatches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, passwordHash);
    }

    public void changePassword(String rawPassword, PasswordEncoder encoder) {
        this.passwordHash = encoder.encode(rawPassword);
    }
}