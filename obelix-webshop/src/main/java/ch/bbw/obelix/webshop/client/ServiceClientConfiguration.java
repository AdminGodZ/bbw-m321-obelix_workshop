package ch.bbw.obelix.webshop.client;

import ch.bbw.obelix.basket.api.BasketApi;
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
public class ServiceClientConfiguration {

	@Bean
	QuarryApi quarryApi(WebClient.Builder builder, @Value("${quarry.base-url}") String baseUrl) {
		return createClient(builder, baseUrl, QuarryApi.class);
	}

	@Bean
	BasketApi basketApi(WebClient.Builder builder, @Value("${basket.base-url}") String baseUrl) {
		return createClient(builder, baseUrl, BasketApi.class);
	}

	private <T> T createClient(WebClient.Builder builder, String baseUrl, Class<T> api) {
		var webClient = builder
			.baseUrl(baseUrl)
			.defaultStatusHandler(HttpStatusCode::is4xxClientError, response -> response.createException()
				.map(error -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service rejected request", error)))
			.build();
		return HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
			.build()
			.createClient(api);
	}
}
