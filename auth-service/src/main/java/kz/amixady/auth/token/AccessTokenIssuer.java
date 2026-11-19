package kz.amixady.auth.token;

import kz.amixady.auth.config.JwtProperties;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AccessTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties props;

    public AccessToken issue(Account account, Session session, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(account.getId().toString())
                .claim(JwtClaims.SESSION_ID, session.getId().toString())
                .claim(JwtClaims.EMAIL_VERIFIED, account.isEmailVerified())
                .issuedAt(now)
                .expiresAt(now.plus(props.accessTokenTtl()))
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(props.keyId())
                .build();

        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(value, props.accessTokenTtl());
    }
}