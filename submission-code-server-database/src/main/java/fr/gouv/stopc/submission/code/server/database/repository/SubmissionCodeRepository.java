package fr.gouv.stopc.submission.code.server.database.repository;

import fr.gouv.stopc.submission.code.server.database.entity.Lot;
import fr.gouv.stopc.submission.code.server.database.entity.SubmissionCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface SubmissionCodeRepository extends PagingAndSortingRepository<SubmissionCode, Long> {
     SubmissionCode findByCodeAndType(String code, String type);

     List<SubmissionCode> findAllByLotkeyIdAndTypeEquals(long lot, String type);

     /**
      * count number of codes in db for the given lot identifier.
      * @param lotIdentifier lot identifier in db
      * @return count number of codes in db for the given lot identifier.
      */
     long countSubmissionCodeByLotkeyId(long lotIdentifier);

     Page<SubmissionCode> findAllByLotkeyId(long lotIdentifier, Pageable pageable);


     SubmissionCode findByCodeAndTypeAndAndDateEndValidityLessThan(String code, String type, OffsetDateTime validityLessThanDate);

     void deleteAllByLotkey(Lot lotkey);
}
