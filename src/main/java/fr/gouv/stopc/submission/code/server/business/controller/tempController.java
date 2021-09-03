package fr.gouv.stopc.submission.code.server.business.controller;

import fr.gouv.stopc.submission.code.server.business.service.schedule.DailyGenerateSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/scheduler")
public class tempController {

    // TODO : to remove, used for dev manual launch

    private DailyGenerateSchedule dailyGenerateSchedule;

    @Autowired
    public tempController(DailyGenerateSchedule dailyGenerateSchedule) {
        this.dailyGenerateSchedule = dailyGenerateSchedule;
    }

    @GetMapping(
            value = "/launch",
            produces = { "application/json" }
    )
    public void launch() {
        dailyGenerateSchedule.dailyProductionCodeScheduler();
    }
}
