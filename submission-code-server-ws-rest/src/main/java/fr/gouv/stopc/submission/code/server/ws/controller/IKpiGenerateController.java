package fr.gouv.stopc.submission.code.server.ws.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;

/**
 * Kpi generation REST controller definition
 * 
 * @author plant-stopcovid
 *
 */
public interface IKpiGenerateController {

	/**
	 * Computes the Kpi on a period
	 * 
	 * @param fromDate beginning date of the period
	 * @param toDate   ending date of the period
	 * @return the list of Kpi computed on this period (one per day)
	 */
	@GetMapping(value = "/kpi")
	ResponseEntity<List<SubmissionCodeServerKpi>> generateKpi(
			@RequestParam(name = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(name = "toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate);

}
