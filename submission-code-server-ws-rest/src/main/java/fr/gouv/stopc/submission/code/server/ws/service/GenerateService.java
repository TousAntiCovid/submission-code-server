package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import fr.gouv.stopc.submission.code.server.commun.service.IUUIDv4CodeService;

import javax.inject.Inject;

public class GenerateService implements IGenerateService {
   private IUUIDv4CodeService uuiDv4CodeService;
   private IAlphaNumericCodeService alphaNumericCodeService;

   @Inject
   public GenerateService (IUUIDv4CodeService uuiDv4CodeService, IAlphaNumericCodeService alphaNumericCodeService){
       this.alphaNumericCodeService= alphaNumericCodeService;
       this.uuiDv4CodeService = uuiDv4CodeService;
   }


   @Override
    public String generateUUIDv4Code() {
       //TODO: Verify that code don't exist in DB before returning
       return this.uuiDv4CodeService.generateCode();
    }

    @Override
    public String generateAlphaNumericCode() {
        //TODO: Verify that code don't exist in DB before returning
        return this.alphaNumericCodeService.generateCode();
    }
}
