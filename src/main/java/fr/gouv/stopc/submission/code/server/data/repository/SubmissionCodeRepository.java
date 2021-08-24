package fr.gouv.stopc.submission.code.server.data.repository;

import fr.gouv.stopc.submission.code.server.data.entity.Lot;
import fr.gouv.stopc.submission.code.server.data.entity.SubmissionCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface SubmissionCodeRepository extends PagingAndSortingRepository<SubmissionCode, Long> {

    SubmissionCode findByCodeAndType(String code, String type);

    List<SubmissionCode> findAllByLotkeyIdAndTypeEquals(long lot, String type);

    /**
     * count number of codes in db for the given lot identifier.
     * 
     * @param lotIdentifier lot identifier in db
     * @return count number of codes in db for the given lot identifier.
     */
    long countSubmissionCodeByLotkeyId(long lotIdentifier);

    Page<SubmissionCode> findAllByLotkeyId(long lotIdentifier, Pageable pageable);

    SubmissionCode findByCodeAndTypeAndAndDateEndValidityLessThan(String code, String type,
            OffsetDateTime validityLessThanDate);

    void deleteAllByLotkey(Lot lotkey);

    /**
     * The method serches the used codes from fromDate until dateTo of type
     * 
     * @param dateFrom
     * @param dateTo
     * @param type
     * @return
     */
    @Query("SELECT COUNT(*) FROM SubmissionCode s where s.type = :type and s.dateUse >= :fromDate and s.dateUse < :toDate")
    long countSubmissionCodeUsedByDate(@Param("fromDate") OffsetDateTime dateFrom,
            @Param("toDate") OffsetDateTime dateTo, @Param("type") String type);

    /**
     * @param dateTime
     * @param type
     * @return
     */
    @Query("SELECT COUNT(*) FROM SubmissionCode s where s.dateUse is NULL and s.type = :type and s.dateEndValidity < :dateExpire")
    long countSubmissionCodeExpiredDate(@Param("dateExpire") OffsetDateTime dateTime, @Param("type") String type);

    @Query("SELECT COUNT(*) FROM SubmissionCode s where s.type = :type and s.dateGeneration >= :startDate and s.dateGeneration < :endDate")
    long countGeneratedCodes(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate,
            @Param("type") String type);
}