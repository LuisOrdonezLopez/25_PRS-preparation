package vallegrande.edu.pe.preparationservice.domain.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.model.Preparation;

public interface PreparationRepositoryPort {
    Flux<Preparation> findByRequestId(Long requestId);
    Flux<Preparation> findByRequestIdAndStatus(Long requestId, String status);
    Mono<Preparation> findById(Long id);
    Mono<Preparation> save(Preparation preparation);
    Mono<Void>        deleteById(Long id);
    Mono<Boolean>     existsByRequestIdAndStatusNot(Long requestId, String status);
}
