package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.business.controller.error.SubmissionCodeServerException;
import fr.gouv.stopc.submission.code.server.business.vo.SubmissionCodeServerKpi;

import java.time.LocalDate;
import java.util.List;

public interface IKpiService {

    List<SubmissionCodeServerKpi> generateKPI(LocalDate dateFrom, LocalDate dateTo)
            throws SubmissionCodeServerException;
}
