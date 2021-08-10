package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.data.entity.SequenceFichier;
import fr.gouv.stopc.submission.code.server.data.repository.SequenceFichierRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class SequenceFichierService {

    private final SequenceFichierRepository repository;

    @Inject
    public SequenceFichierService(SequenceFichierRepository repository) {
        this.repository = repository;
    }

    public Optional<SequenceFichier> getSequence(OffsetDateTime date) {

        if (Objects.nonNull(date)) {
            return Optional.ofNullable(
                    this.repository
                            .findByAnneeAndMoisAndJour(date.getYear(), date.getMonthValue(), date.getDayOfMonth())
                            .map(sequenceFichier -> {
                                sequenceFichier.setSequence(sequenceFichier.getSequence() + 1);
                                this.repository.saveAndFlush(sequenceFichier);
                                return sequenceFichier;
                            })
                            .orElseGet(() -> this.createSequence(date))
            );
        }
        return Optional.empty();
    }

    private SequenceFichier createSequence(OffsetDateTime date) {
        int shortYear = (date.getYear() % 100);
        int sequence = (shortYear + 1) % 100;
        return this.repository.saveAndFlush(
                SequenceFichier.builder()
                        .annee(date.getYear())
                        .mois(date.getMonthValue())
                        .jour(date.getDayOfMonth())
                        .sequence(sequence).build()
        );
    }
}
