package fr.gouv.stopc.submission.code.server.business.service;

import fr.gouv.stopc.submission.code.server.business.service.schedule.DailyGenerateSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DailyGenerateRunnerService implements CommandLineRunner {

    private final DailyGenerateSchedule dailyGenerateSchedule;

    @Override
    public void run(final String... args) {
        dailyGenerateSchedule.dailyProductionCodeScheduler();
    }
}
