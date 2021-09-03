package fr.gouv.stopc.submission.code.server.business.service.schedule;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "stop.covid.qr.code.cron")
public class GenerationConfigProperties {
    List<GenerationConfig> scheduling;
}
