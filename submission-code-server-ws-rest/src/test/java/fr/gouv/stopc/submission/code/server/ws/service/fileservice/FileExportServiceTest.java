package fr.gouv.stopc.submission.code.server.ws.service.fileservice;

import fr.gouv.stopc.submission.code.server.ws.service.FileExportServiceImpl;
import fr.gouv.stopc.submission.code.server.ws.service.GenerateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class FileExportServiceTest {

    @Autowired
    private GenerateServiceImpl generateService;

    @Autowired
    private FileExportServiceImpl fileExportService;


    @Before
    public void init(){
        log.info("Initialize mokito injection in services...");
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createZipComplete(){
       //initValues codes in table
       //TODO

        // String numberCodeDay, String lot, String dateFrom, String dateTo

        //fileExportService.zipExport()





    }
}
