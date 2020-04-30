package fr.gouv.stopc.submission.code.server.database.repository;

import fr.gouv.stopc.submission.code.server.database.entity.CodePositive;
import org.springframework.data.repository.CrudRepository;

public interface CodePositiveRepository extends CrudRepository<CodePositive, Long> {
     CodePositive findByCode(String code);
}
