package ch.bbw.obelix.quarry;

import ch.bbw.obelix.quarry.repository.MenhirRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuarryMetricsConfiguration {

	@Bean
	Gauge menhirCount(MeterRegistry registry, MenhirRepository repository) {
		return Gauge.builder("quarry.menhirs", repository, MenhirRepository::count)
			.description("Number of menhirs available in the quarry")
			.register(registry);
	}
}
