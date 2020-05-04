package fr.gouv.stopc.submission.code.server.ws.controller;

import fr.gouv.stopc.submission.code.server.ws.dto.ViewLotInformationDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;

@RestController
@RequestMapping(value = "${controller.path.prefix}/views")
@Produces(MediaType.APPLICATION_JSON_VALUE)
public interface IViewController {


    @GetMapping(path="/lots/{lotIdentifier}/information")
    ResponseEntity<ViewLotInformationDto> getLotInformation(@PathVariable long lotIdentifier);
    
}
