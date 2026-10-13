package vallegrande.edu.pe.preparationservice.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import vallegrande.edu.pe.preparationservice.domain.exception.PreparationNotFoundException;
import vallegrande.edu.pe.preparationservice.domain.model.Preparation;
import vallegrande.edu.pe.preparationservice.domain.port.out.PreparationRepositoryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.RequestQueryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.TariffQueryPort;
import vallegrande.edu.pe.preparationservice.domain.port.out.TenantIdPort;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreparationServiceTest {

    @Mock
    private PreparationRepositoryPort repositoryPort;
    @Mock
    private RequestQueryPort requestQueryPort;
    @Mock
    private TariffQueryPort tariffQueryPort;
    @Mock
    private TenantIdPort tenantIdPort;

    private PreparationService service;

    @BeforeEach
    void setUp() {
        service = new PreparationService(repositoryPort, requestQueryPort, tariffQueryPort, tenantIdPort);
    }

    @Test
    void saveAssignsTenantAndPendingStatus() {
        Preparation preparation = new Preparation();
        preparation.setRequestId(10L);
        preparation.setType("TITULAR");
        preparation.setSessionDate(LocalDate.of(2026, 10, 20));

        when(tenantIdPort.currentTenantId()).thenReturn(Mono.just(7L));
        when(repositoryPort.save(any(Preparation.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.save(preparation))
                .assertNext(saved -> {
                    assertThat(saved.getTenantId()).isEqualTo("7");
                    assertThat(saved.getStatus()).isEqualTo("PENDING");
                    assertThat(saved.getCreatedAt()).isNotNull();
                    assertThat(saved.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void allSessionsCompletedIsTrueOnlyWhenThereAreCompletedSessions() {
        Preparation completed = new Preparation();
        completed.setStatus("COMPLETED");
        Preparation pending = new Preparation();
        pending.setStatus("PENDING");

        when(repositoryPort.findByRequestId(10L)).thenReturn(Flux.just(completed));
        StepVerifier.create(service.allSessionsCompleted(10L))
                .expectNext(true)
                .verifyComplete();

        when(repositoryPort.findByRequestId(11L)).thenReturn(Flux.just(completed, pending));
        StepVerifier.create(service.allSessionsCompleted(11L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void allSessionsCompletedIsFalseWhenThereAreNoSessions() {
        when(repositoryPort.findByRequestId(99L)).thenReturn(Flux.empty());

        StepVerifier.create(service.allSessionsCompleted(99L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findByIdReportsMissingPreparation() {
        when(repositoryPort.findById(404L)).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(404L))
                .expectError(PreparationNotFoundException.class)
                .verify();
    }
}
