package fr.gouv.stopc.submission.code.server.ws.controller.impl;

import fr.gouv.stopc.submission.code.server.ws.controller.ICsvController;
import fr.gouv.stopc.submission.code.server.ws.service.ICsvService;
import fr.gouv.stopc.submission.code.server.ws.vo.RequestCsvVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import java.io.File;

@Service
@Slf4j
public class CsvControllerImpl implements ICsvController {
    private ICsvService csvService;

    @Inject
    public CsvControllerImpl(ICsvService csvService) {
        this.csvService = csvService;
    }


    public ResponseEntity readCsvByLot(RequestCsvVo requestCsvVo) {
        try {
            File file = csvService.csvExport(requestCsvVo.getLot());
            if (file == null) {
                String message = "The lot is not exist";
                return ResponseEntity.badRequest().body(message);
            }
            String csvName = file.getName();
            return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=" + csvName + ".csv").contentLength(file.length())
                    .contentType(MediaType.parseMediaType("text/csv")).body(new FileSystemResource(file));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to generate report");
        }
    }
}
