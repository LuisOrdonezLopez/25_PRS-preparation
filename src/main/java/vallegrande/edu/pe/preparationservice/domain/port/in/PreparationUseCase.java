package vallegrande.edu.pe.preparationservice.domain.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.model.Preparation;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationReadiness;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationTariff;

public interface PreparationUseCase {
    Flux<Preparation> findByRequestId(Long requestId);
    Flux<Preparation> findByRequestIdAndStatus(Long requestId, String status);
    Mono<Preparation> findById(Long id);
    Mono<Preparation> save(Preparation preparation);
    Mono<Preparation> update(Long id, Preparation preparation);
    Mono<Preparation> changeStatus(Long id, String status);
    Mono<Void>        delete(Long id);
    Mono<Boolean>     allSessionsCompleted(Long requestId);
    Mono<PreparationTariff> findTariffByRequestId(Long requestId);
    Mono<PreparationReadiness> readinessForCelebration(Long requestId);
}
