package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.data.entity.SequenceFichier;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ISequenceFichierService {

    Optional<SequenceFichier> getSequence(OffsetDateTime date);

}
