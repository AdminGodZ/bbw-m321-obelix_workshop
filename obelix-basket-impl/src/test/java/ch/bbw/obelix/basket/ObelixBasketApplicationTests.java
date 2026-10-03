package ch.bbw.obelix.basket;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import ch.bbw.obelix.basket.dto.BasketDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ObelixBasketApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void offerReadAndClearBasket() {
		assertEmptyBasket();
		var boar = new BasketDto.BasketItem("boar", 2);
		var honey = new BasketDto.BasketItem("honey", 1);
		webTestClient.put().uri("/api/basket/offer").bodyValue(boar).exchange()
			.expectStatus().isOk()
			.expectBody(BasketDto.class).isEqualTo(new BasketDto(List.of(boar)));
		webTestClient.put().uri("/api/basket/offer").bodyValue(honey).exchange()
			.expectStatus().isOk()
			.expectBody(BasketDto.class).isEqualTo(new BasketDto(List.of(boar, honey)));
		webTestClient.get().uri("/api/basket").exchange()
			.expectStatus().isOk()
			.expectBody(BasketDto.class).isEqualTo(new BasketDto(List.of(boar, honey)));
		webTestClient.delete().uri("/api/basket").exchange().expectStatus().isOk();
		assertEmptyBasket();
	}

	@Test
	void invalidOfferIsRejected() {
		webTestClient.put().uri("/api/basket/offer")
			.contentType(MediaType.APPLICATION_JSON).bodyValue("{\"name\":\"boar\",\"count\":0}")
			.exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/api/basket/offer")
			.contentType(MediaType.APPLICATION_JSON).bodyValue("{\"name\":\"\",\"count\":1}")
			.exchange().expectStatus().isBadRequest();
	}

	private void assertEmptyBasket() {
		webTestClient.get().uri("/api/basket").exchange()
			.expectStatus().isOk()
			.expectBody(BasketDto.class)
			.value(basket -> assertThat(basket.items()).isEmpty());
	}
}
