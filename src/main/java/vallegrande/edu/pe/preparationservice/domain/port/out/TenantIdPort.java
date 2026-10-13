package vallegrande.edu.pe.preparationservice.domain.port.out;

import reactor.core.publisher.Mono;

/** Obtiene el tenant del contexto autenticado sin filtrar infraestructura a la aplicación. */
public interface TenantIdPort {

    Mono<Long> currentTenantId();
}
