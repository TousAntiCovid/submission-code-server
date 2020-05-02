package fr.gouv.stopc.submission.code.server.database.repository;

import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubmissionCodeRepository extends CrudRepository<SubmissionCode, Long> {
     SubmissionCode findByCodeAndType(String code, String type);

     List<SubmissionCode> findAllByLotNullAndTypeEquals(String type);

     @Query(value = "SELECT max(lot) FROM SubmissionCode ")
     long lastLot();

}
