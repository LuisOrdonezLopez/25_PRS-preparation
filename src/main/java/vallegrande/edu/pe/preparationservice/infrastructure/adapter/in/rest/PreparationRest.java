package vallegrande.edu.pe.preparationservice.infrastructure.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.port.in.PreparationUseCase;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.request.PreparationRequest;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.response.PreparationReadinessResponse;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.response.PreparationResponse;
import vallegrande.edu.pe.preparationservice.infrastructure.dto.response.PreparationTariffResponse;
import vallegrande.edu.pe.preparationservice.infrastructure.mapper.PreparationDtoMapper;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/preparations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PreparationRest {

    private final PreparationUseCase useCase;

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @GetMapping("/request/{requestId}")
    public Flux<PreparationResponse> findByRequest(@PathVariable Long requestId) {
        return useCase.findByRequestId(requestId).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @GetMapping("/request/{requestId}/status/{status}")
    public Flux<PreparationResponse> findByRequestAndStatus(@PathVariable Long requestId, @PathVariable String status) {
        return useCase.findByRequestIdAndStatus(requestId, status).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @GetMapping("/{id}")
    public Mono<PreparationResponse> findById(@PathVariable Long id) {
        return useCase.findById(id).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @GetMapping("/request/{requestId}/all-completed")
    public Mono<Map<String, Boolean>> allCompleted(@PathVariable Long requestId) {
        return useCase.allSessionsCompleted(requestId).map(r -> Map.of("allCompleted", r));
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @GetMapping("/request/{requestId}/tariff")
    public Mono<PreparationTariffResponse> tariff(@PathVariable Long requestId) {
        return useCase.findTariffByRequestId(requestId).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @GetMapping("/request/{requestId}/readiness")
    public Mono<PreparationReadinessResponse> readiness(@PathVariable Long requestId) {
        return useCase.readinessForCelebration(requestId).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Mono<PreparationResponse> save(@RequestBody PreparationRequest p) {
        return useCase.save(PreparationDtoMapper.toDomain(p)).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @PutMapping("/{id}")
    public Mono<PreparationResponse> update(@PathVariable Long id, @RequestBody PreparationRequest p) {
        return useCase.update(id, PreparationDtoMapper.toDomain(p)).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @PatchMapping("/{id}/status")
    public Mono<PreparationResponse> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return useCase.changeStatus(id, body.get("status")).map(PreparationDtoMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('SECRETARIO','PARROCO')")
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) { return useCase.delete(id); }
}
