package kz.amixady.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class UserHeadersFilter implements GlobalFilter, Ordered {

    public static final String USER_ID = "X-User-Id";
    public static final String EMAIL_VERIFIED = "X-Email-Verified";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return currentJwt()
                .map(jwt -> withUser(exchange, jwt))
                .switchIfEmpty(Mono.fromSupplier(() -> withoutUser(exchange)))
                .flatMap(chain::filter);
    }

    private Mono<Jwt> currentJwt() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken());
    }

    private ServerWebExchange withUser(ServerWebExchange exchange, Jwt jwt) {
        boolean emailVerified = Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"));
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.set(USER_ID, jwt.getSubject());
                    headers.set(EMAIL_VERIFIED, String.valueOf(emailVerified));
                }))
                .build();
    }

    private ServerWebExchange withoutUser(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(USER_ID);
                    headers.remove(EMAIL_VERIFIED);
                }))
                .build();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}