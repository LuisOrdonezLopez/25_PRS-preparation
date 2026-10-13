package vallegrande.edu.pe.preparationservice.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.port.out.TenantIdPort;

/** Adaptador que lee el tenant desde los claims admitidos por Keycloak. */
@Component
public class JwtTenantContext implements TenantIdPort {

    @Override
    public Mono<Long> currentTenantId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> authentication.getPrincipal() instanceof Jwt)
                .map(authentication -> (Jwt) authentication.getPrincipal())
                .map(this::tenantIdFrom);
    }

    private Long tenantIdFrom(Jwt jwt) {
        Object claim = jwt.getClaim("tenantId");
        if (claim == null) claim = jwt.getClaim("organizationId");
        if (claim == null) claim = jwt.getClaim("tenant_id");
        if (claim == null) throw new IllegalStateException("No tenant_id found in JWT token");
        return Long.valueOf(claim.toString());
    }
}
