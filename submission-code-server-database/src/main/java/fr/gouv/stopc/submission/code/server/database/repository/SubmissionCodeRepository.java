package fr.gouv.stopc.submission.code.server.database.repository;

import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SubmissionCodeRepository extends PagingAndSortingRepository<SubmissionCode, Long> {
     SubmissionCode findByCodeAndType(String code, String type);

     List<SubmissionCode> findAllByLotAndTypeEquals(long lot, String type);

     @Query(value = "SELECT max(lot) FROM SubmissionCode ")
     String lastLot();

     /**
      * count number of codes in db for the given lot identifier.
      * @param lotIdentifier lot identifier in db
      * @return count number of codes in db for the given lot identifier.
      */
     long countSubmissionCodeByLot(long lotIdentifier);

     Page<SubmissionCode> findAllByLot(long lotIdentifier, Pageable pageable);


}
