package vallegrande.edu.pe.preparationservice.domain.port.out;

import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.model.PreparationTariff;

/** Contrato de salida para consultar la tarifa vigente de un sacramento. */
public interface TariffQueryPort {

    Mono<PreparationTariff> findCurrent(RequestQueryPort.RequestData request);
}
