package fr.gouv.stopc.submission.code.server.ws.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Aggregation of Kpi reflecting the usage of codes
 * 
 * @author plant-stopcovid
 *
 */
@Data
@Builder
public class SubmissionCodeServerKpi {

	/**
	 * Date of computation
	 */
	private LocalDate date;

	/**
	 * Number of short codes used
	 */
	private Long nbShortCodesUsed;

	/**
	 * Number of long codes used
	 */
	private Long nbLongCodesUsed;

	/**
	 * Number of long codes expired
	 */
	private Long nbLongExpiredCodes;

	/**
	 * Number of short codes expired
	 */
	private Long nbShortExpiredCodes;

}
