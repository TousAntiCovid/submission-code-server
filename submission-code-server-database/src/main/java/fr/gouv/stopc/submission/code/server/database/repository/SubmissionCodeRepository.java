package fr.gouv.stopc.submission.code.server.database.repository;

import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import org.springframework.data.repository.CrudRepository;

public interface SubmissionCodeRepository extends CrudRepository<SubmissionCode, Long> {
     SubmissionCode findByCodeAndType(String code, String type);
}
