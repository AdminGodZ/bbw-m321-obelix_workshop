package ch.bbw.obelix.webshop.client;

import ch.bbw.obelix.quarry.api.QuarryApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class QuarryClientConfiguration {

	@Bean
	QuarryApi quarryApi(WebClient.Builder builder, @Value("${quarry.base-url}") String baseUrl) {
		var webClient = builder
			.baseUrl(baseUrl)
			.defaultStatusHandler(HttpStatusCode::is4xxClientError, response -> response.createException()
				.map(error -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown menhir", error)))
			.build();
		return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
			.build()
			.createClient(QuarryApi.class);
	}
}
