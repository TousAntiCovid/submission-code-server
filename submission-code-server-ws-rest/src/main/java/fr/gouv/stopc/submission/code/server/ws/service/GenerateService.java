package fr.gouv.stopc.submission.code.server.ws.service;

import fr.gouv.stopc.submission.code.server.commun.service.IAlphaNumericCodeService;
import fr.gouv.stopc.submission.code.server.commun.service.IUUIDv4CodeService;

import javax.inject.Inject;

public class GenerateService implements IGenerateService {
   private IUUIDv4CodeService iuuiDv4CodeService;
   private IAlphaNumericCodeService iAlphaNumericCodeService;

   @Inject
   public GenerateService (IUUIDv4CodeService iuuiDv4CodeService, IAlphaNumericCodeService iAlphaNumericCodeService){
       this.iAlphaNumericCodeService= iAlphaNumericCodeService;
       this.iuuiDv4CodeService = iuuiDv4CodeService;
   }


   @Override
    public String generateUUIDv4Code() {
        return null;
    }

    @Override
    public String generateAlphaNumercCode() {
        return null;
    }
}
