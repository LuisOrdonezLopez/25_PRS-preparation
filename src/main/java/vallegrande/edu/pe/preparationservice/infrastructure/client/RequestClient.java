package vallegrande.edu.pe.preparationservice.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.port.out.RequestQueryPort;

import java.util.UUID;

/** Minimal request contract needed to resolve the sacrament tariff. */
@Component
public class RequestClient implements RequestQueryPort {
    private final WebClient webClient;

    public RequestClient(@Value("${services.requests.url:http://localhost:8087}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    @Override
    public Mono<RequestData> findById(Long requestId) {
        return bearerToken()
                .flatMap(token -> webClient.get()
                        .uri("/api/v1/requests/{id}", requestId)
                        .headers(headers -> {
                            if (!token.isBlank()) headers.setBearerAuth(token);
                        })
                        .retrieve()
                        .bodyToMono(RequestSummary.class)
                        .map(r -> new RequestData(r.id(), r.tenantId(), r.userId(), r.requestType(), r.sacramentId())));
    }

    private Mono<String> bearerToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(authentication -> ((JwtAuthenticationToken) authentication).getToken().getTokenValue())
                .defaultIfEmpty("");
    }

    private record RequestSummary(Long id, Long tenantId, Long userId, String requestType, UUID sacramentId) {}
}
