package vallegrande.edu.pe.preparationservice.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.preparationservice.domain.model.Preparation;
import vallegrande.edu.pe.preparationservice.domain.port.out.PreparationRepositoryPort;

@Component @RequiredArgsConstructor
public class PreparationPersistenceAdapter implements PreparationRepositoryPort {

    private final PreparationR2dbcRepo repo;

    private Preparation toDomain(PreparationEntity e) {
        Preparation p = new Preparation();
        p.setId(e.getId()); p.setRequestId(e.getRequestId()); p.setType(e.getType());
        p.setSessionDate(e.getSessionDate()); p.setStatus(e.getStatus());
        p.setAttendanceStatus(e.getAttendanceStatus());
        p.setPaymentAmount(e.getPaymentAmount());
        p.setPaymentStatus(e.getPaymentStatus());
        p.setPaymentDate(e.getPaymentDate());
        p.setPaymentReceiptUrl(e.getPaymentReceiptUrl());
        p.setExamScore(e.getExamScore());
        p.setExamPassed(e.getExamPassed());
        p.setNotes(e.getNotes());
        p.setCreatedAt(e.getCreatedAt()); p.setUpdatedAt(e.getUpdatedAt());
        p.setTenantId(e.getTenantId());
        return p;
    }

    private PreparationEntity toEntity(Preparation p) {
        PreparationEntity e = new PreparationEntity();
        e.setId(p.getId()); e.setRequestId(p.getRequestId()); e.setType(p.getType());
        e.setSessionDate(p.getSessionDate()); e.setStatus(p.getStatus());
        e.setAttendanceStatus(p.getAttendanceStatus());
        e.setPaymentAmount(p.getPaymentAmount());
        e.setPaymentStatus(p.getPaymentStatus());
        e.setPaymentDate(p.getPaymentDate());
        e.setPaymentReceiptUrl(p.getPaymentReceiptUrl());
        e.setExamScore(p.getExamScore());
        e.setExamPassed(p.getExamPassed());
        e.setNotes(p.getNotes());
        e.setCreatedAt(p.getCreatedAt()); e.setUpdatedAt(p.getUpdatedAt());
        e.setTenantId(p.getTenantId());
        return e;
    }

    @Override public Flux<Preparation> findByRequestId(Long id)                              { return repo.findByRequestId(id).map(this::toDomain); }
    @Override public Flux<Preparation> findByRequestIdAndStatus(Long id, String s)           { return repo.findByRequestIdAndStatus(id, s).map(this::toDomain); }
    @Override public Mono<Preparation> findById(Long id)                                     { return repo.findById(id).map(this::toDomain); }
    @Override public Mono<Preparation> save(Preparation p)                                   { return repo.save(toEntity(p)).map(this::toDomain); }
    @Override public Mono<Void>        deleteById(Long id)                                   { return repo.deleteById(id); }
    @Override public Mono<Boolean>     existsByRequestIdAndStatusNot(Long id, String status) { return repo.existsByRequestIdAndStatusNot(id, status); }
}
