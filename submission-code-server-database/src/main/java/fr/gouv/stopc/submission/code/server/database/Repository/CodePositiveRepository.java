package fr.gouv.stopc.submission.code.server.database.Repository;

import fr.gouv.stopc.submission.code.server.database.Entity.CodePositive;
import org.springframework.data.repository.CrudRepository;

public interface CodePositiveRepository extends CrudRepository<CodePositive, Long> {
     CodePositive findByCode(String code);
}
