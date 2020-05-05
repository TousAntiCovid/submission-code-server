package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotCodeDetailPageDto;
import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotInformationDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;

@CrossOrigin
@RestController
@RequestMapping(value = "${controller.path.prefix}/views")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IViewController {


    @GetMapping(path="/lots/{lotIdentifier}/information")
    ResponseEntity<ViewLotInformationDto> getLotInformation(@PathVariable long lotIdentifier);

    @GetMapping(path="/lots/{lotIdentifier}/page/{page}/by/{elementByPage}")
    ResponseEntity<ViewLotCodeDetailPageDto> getCodeValuesForPage(
            @PathVariable long lotIdentifier,
            @PathVariable int page,
            @PathVariable int elementByPage
    );
}
