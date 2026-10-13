package vallegrande.edu.pe.preparationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.exception.PreparationNotFoundException;
import vallegrande.edu.pe.preparationservice.domain.model.Preparation;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationReadiness;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationTariff;
import vallegrande.edu.pe.preparationservice.domain.port.in.PreparationUseCase;
import vallegrande.edu.pe.preparationservice.domain.port.out.PreparationRepositoryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.RequestQueryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.TariffQueryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.TenantIdPort;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PreparationService implements PreparationUseCase {

    private final PreparationRepositoryPort repositoryPort;
    private final RequestQueryPort requestQueryPort;
    private final TariffQueryPort tariffQueryPort;
    private final TenantIdPort tenantIdPort;

    @Override public Flux<Preparation> findByRequestId(Long id) { return repositoryPort.findByRequestId(id); }
    @Override public Flux<Preparation> findByRequestIdAndStatus(Long id, String s) { return repositoryPort.findByRequestIdAndStatus(id, s); }

    @Override
    public Mono<Preparation> findById(Long id) {
        return repositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new PreparationNotFoundException(id)));
    }

    @Override
    public Mono<Preparation> save(Preparation p) {
        return tenantIdPort.currentTenantId().flatMap(tenantId -> {
            p.setTenantId(String.valueOf(tenantId));
            p.setStatus("PENDING");
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return repositoryPort.save(p);
        });
    }

    @Override
    public Mono<Preparation> update(Long id, Preparation p) {
        return findById(id).flatMap(existing -> {
            existing.setType(p.getType());
            existing.setSessionDate(p.getSessionDate());
            existing.setNotes(p.getNotes());
            existing.setUpdatedAt(LocalDateTime.now());
            return repositoryPort.save(existing);
        });
    }

    @Override
    public Mono<Preparation> changeStatus(Long id, String status) {
        return findById(id).flatMap(existing -> {
            existing.setStatus(status);
            existing.setUpdatedAt(LocalDateTime.now());
            return repositoryPort.save(existing);
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        return findById(id).flatMap(p -> repositoryPort.deleteById(id));
    }

    @Override
    public Mono<Boolean> allSessionsCompleted(Long requestId) {
        return repositoryPort.findByRequestId(requestId)
                .collectList()
                .map(sessions -> !sessions.isEmpty()
                        && sessions.stream().allMatch(session -> "COMPLETED".equals(session.getStatus())));
    }

    @Override
    public Mono<PreparationTariff> findTariffByRequestId(Long requestId) {
        return requestQueryPort.findById(requestId)
                .flatMap(tariffQueryPort::findCurrent)
                .switchIfEmpty(Mono.just(PreparationTariff.unavailable(
                        "No se encontró la solicitud o no existe una tarifa vigente")))
                .onErrorReturn(PreparationTariff.unavailable(
                        "No fue posible consultar la solicitud o la tarifa vigente"));
    }

    @Override
    public Mono<PreparationReadiness> readinessForCelebration(Long requestId) {
        Mono<Boolean> sessions = allSessionsCompleted(requestId);
        Mono<PreparationTariff> tariff = findTariffByRequestId(requestId);
        return Mono.zip(sessions, tariff)
                .map(result -> PreparationReadiness.builder()
                        .allSessionsCompleted(result.getT1())
                        .tariffAvailable(result.getT2().isAvailable())
                        .canProceed(result.getT1() && result.getT2().isAvailable())
                        .tariff(result.getT2())
                        .build());
    }
}
