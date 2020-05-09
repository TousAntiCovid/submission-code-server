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

     List<SubmissionCode> findAllByLotkey_IdAndTypeEquals(long lot, String type);

     /**
      * count number of codes in db for the given lot identifier.
      * @param lotIdentifier lot identifier in db
      * @return count number of codes in db for the given lot identifier.
      */
     long countSubmissionCodeByLotkey_Id(long lotIdentifier);

     Page<SubmissionCode> findAllByLotkey_Id(long lotIdentifier, Pageable pageable);


}
