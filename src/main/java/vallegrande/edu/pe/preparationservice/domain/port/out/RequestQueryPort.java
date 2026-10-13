package vallegrande.edu.pe.preparationservice.domain.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/** Contrato mínimo para consultar la solicitud que origina una preparación. */
public interface RequestQueryPort {

    Mono<RequestData> findById(Long requestId);

    record RequestData(Long id, Long tenantId, Long userId, String requestType, UUID sacramentId) {
    }
}
