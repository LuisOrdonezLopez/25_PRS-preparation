package vallegrande.edu.pe.preparationservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;
import java.util.*;
import java.util.stream.Collectors;

@Configuration @EnableWebFluxSecurity @EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.csrf(c -> c.disable())
                .authorizeExchange(e -> e.pathMatchers("/swagger-ui/**","/v3/api-docs/**","/api-docs/**","/webjars/**").permitAll().anyExchange().authenticated())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter())))
                .build();
    }
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> converter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(jwt -> {
            @SuppressWarnings("unchecked") Map<String,Object> ra = (Map<String,Object>) jwt.getClaims().get("realm_access");
            if (ra == null) return List.of();
            @SuppressWarnings("unchecked") Collection<String> roles = (Collection<String>) ra.get("roles");
            if (roles == null) return List.of();
            return roles.stream().map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())).collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(c);
    }
}
