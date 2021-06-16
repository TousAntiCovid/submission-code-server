package fr.gouv.stopc.submission.code.server.database.service;

import fr.gouv.stopc.submission.code.server.database.entity.SequenceFichier;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ISequenceFichierService {

    Optional<SequenceFichier> getSequence(OffsetDateTime date);

}
