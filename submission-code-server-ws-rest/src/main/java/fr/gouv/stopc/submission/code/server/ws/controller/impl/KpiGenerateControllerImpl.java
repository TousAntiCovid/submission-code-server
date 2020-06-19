package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import java.time.LocalDate;
import java.util.List;

import javax.ws.rs.Produces;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.stopc.submission.code.server.ws.controller.IKpiGenerateController;
import fr.gouv.stopc.submission.code.server.ws.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.ws.service.IKpiService;
import fr.gouv.stopc.submission.code.server.ws.vo.SubmissionCodeServerKpi;

/**
 * Default implementation of the Kpi generation REST Controller
 * 
 * @author plant-stopcovid
 *
 */
@RestController
@RequestMapping(value = "${controller.path.prefix}")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public class KpiGenerateControllerImpl implements IKpiGenerateController {

	/**
	 * The Kpi generation service
	 */
	private IKpiService ikpiService;

	/**
	 * Spring Injection constructor
	 * 
	 * @param ikpiService the <code>IKpiService</code> bean instance to inject
	 */
	public KpiGenerateControllerImpl(IKpiService ikpiService) {
		this.ikpiService = ikpiService;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity generateKpi(LocalDate fromDate, LocalDate toDate) {
		List<SubmissionCodeServerKpi> result;
		try {
			result = ikpiService.generateKPI(fromDate, toDate);
		} catch (SubmissionCodeServerException s) {
			return ResponseEntity.badRequest().body(s.getMessage());
		}

		return ResponseEntity.ok(result);
	}
}
