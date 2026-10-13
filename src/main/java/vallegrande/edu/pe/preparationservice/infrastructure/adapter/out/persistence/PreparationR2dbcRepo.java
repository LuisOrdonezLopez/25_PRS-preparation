package vallegrande.edu.pe.preparationservice.infrastructure.adapter.out.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PreparationR2dbcRepo extends ReactiveCrudRepository<PreparationEntity, Long> {
    Flux<PreparationEntity> findByRequestId(Long requestId);
    Flux<PreparationEntity> findByRequestIdAndStatus(Long requestId, String status);
    Mono<Boolean> existsByRequestIdAndStatusNot(Long requestId, String status);
}
