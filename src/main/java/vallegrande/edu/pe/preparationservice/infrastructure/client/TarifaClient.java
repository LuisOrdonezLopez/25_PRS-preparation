package vallegrande.edu.pe.preparationservice.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationTariff;
import vallegrande.edu.pe.preparationservice.domain.port.out.RequestQueryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.TariffQueryPort;

import java.util.Locale;
import java.util.UUID;

/** Read-only integration with vg-ms-tarifas. */
@Component
public class TarifaClient implements TariffQueryPort {
    private final WebClient webClient;

    public TarifaClient(@Value("${services.tarifas.url:http://localhost:8100}") String url) {
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    @Override
    public Mono<PreparationTariff> findCurrent(RequestQueryPort.RequestData request) {
        return bearerToken().flatMap(token -> {
            if (request.sacramentId() != null) {
                return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/api/tarifas/vigente")
                                .queryParam("tipoServicio", "SACRAMENTO")
                                .queryParam("idServicio", request.sacramentId())
                                .build())
                        .headers(headers -> {
                            if (!token.isBlank()) headers.setBearerAuth(token);
                        })
                        .retrieve()
                        .bodyToMono(TarifaResponse.class)
                        .map(this::toDomain);
            }

            // Backward-compatible fallback while existing clients still send only requestType.
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/tarifas")
                            .queryParam("tipoServicio", "SACRAMENTO")
                            .queryParam("estado", "A")
                            .build())
                    .headers(headers -> {
                        if (!token.isBlank()) headers.setBearerAuth(token);
                    })
                    .retrieve()
                    .bodyToFlux(TarifaResponse.class)
                    .filter(tarifa -> matchesRequestType(tarifa.sacramentName(), request.requestType()))
                    .next()
                    .map(this::toDomain);
        }).onErrorResume(error -> Mono.just(PreparationTariff.unavailable(
                "No fue posible consultar la tarifa vigente")));
    }

    private boolean matchesRequestType(String sacramentName, String requestType) {
        if (sacramentName == null || requestType == null) return false;
        String name = normalize(sacramentName);
        return switch (requestType.toUpperCase(Locale.ROOT)) {
            case "BAPTISM" -> name.contains("BAUTISMO") || name.contains("BAPTISM");
            case "CONFIRMATION" -> name.contains("CONFIRMACION") || name.contains("CONFIRMATION");
            case "MARRIAGE" -> name.contains("MATRIMONIO") || name.contains("MARRIAGE");
            default -> false;
        };
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
    }

    private PreparationTariff toDomain(TarifaResponse tarifa) {
        return PreparationTariff.builder()
                .available(true)
                .tariffId(tarifa.tariffId())
                .sacramentId(tarifa.sacramentId())
                .sacramentName(tarifa.sacramentName())
                .amount(tarifa.amount())
                .currency(tarifa.currency())
                .donation(tarifa.donation())
                .status(tarifa.status())
                .message("Tarifa vigente")
                .build();
    }

    private Mono<String> bearerToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(authentication -> ((JwtAuthenticationToken) authentication).getToken().getTokenValue())
                .defaultIfEmpty("");
    }

    private record TarifaResponse(UUID tariffId, Long tenantId, String tipoServicio, UUID sacramentId,
                                  String sacramentName, String nombreTipoMisa,
                                  java.math.BigDecimal amount, String currency, Boolean donation,
                                  java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin, String status) {
        @com.fasterxml.jackson.annotation.JsonCreator
        TarifaResponse(@com.fasterxml.jackson.annotation.JsonProperty("idTarifa") UUID tariffId,
                       @com.fasterxml.jackson.annotation.JsonProperty("tenantId") Long tenantId,
                       @com.fasterxml.jackson.annotation.JsonProperty("tipoServicio") String tipoServicio,
                       @com.fasterxml.jackson.annotation.JsonProperty("idServicio") UUID sacramentId,
                       @com.fasterxml.jackson.annotation.JsonProperty("nombreSacramento") String sacramentName,
                       @com.fasterxml.jackson.annotation.JsonProperty("nombreTipoMisa") String nombreTipoMisa,
                       @com.fasterxml.jackson.annotation.JsonProperty("monto") java.math.BigDecimal amount,
                       @com.fasterxml.jackson.annotation.JsonProperty("moneda") String currency,
                       @com.fasterxml.jackson.annotation.JsonProperty("esDonacion") Boolean donation,
                       @com.fasterxml.jackson.annotation.JsonProperty("fechaInicio") java.time.LocalDate fechaInicio,
                       @com.fasterxml.jackson.annotation.JsonProperty("fechaFin") java.time.LocalDate fechaFin,
                       @com.fasterxml.jackson.annotation.JsonProperty("estado") String status) {
            this.tariffId = tariffId;
            this.tenantId = tenantId;
            this.tipoServicio = tipoServicio;
            this.sacramentId = sacramentId;
            this.sacramentName = sacramentName;
            this.nombreTipoMisa = nombreTipoMisa;
            this.amount = amount;
            this.currency = currency;
            this.donation = donation;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.status = status;
        }
    }
}
