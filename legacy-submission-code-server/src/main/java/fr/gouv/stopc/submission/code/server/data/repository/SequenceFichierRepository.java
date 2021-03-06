package fr.gouv.stopc.submission.code.server.data.repository;

import fr.gouv.stopc.submission.code.server.data.entity.SequenceFichier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SequenceFichierRepository extends JpaRepository<SequenceFichier, Long> {

    Optional<SequenceFichier> findByAnnee(int annee);

    Optional<SequenceFichier> findByAnneeAndMoisAndJour(int annee, int mois, int jour);
}
