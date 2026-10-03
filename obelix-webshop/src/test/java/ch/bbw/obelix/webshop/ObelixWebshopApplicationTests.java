package ch.bbw.obelix.webshop;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.UUID;

import ch.bbw.obelix.basket.api.BasketApi;
import ch.bbw.obelix.basket.dto.BasketDto;
import ch.bbw.obelix.quarry.api.QuarryApi;
import ch.bbw.obelix.quarry.dto.DecorativenessDto;
import ch.bbw.obelix.quarry.dto.MenhirDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ObelixWebshopApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private QuarryApi quarryApi;

	@MockitoBean
	private BasketApi basketApi;

	@Test
	void oldMenhirEndpointsStillWork() {
		var menhirId = UUID.randomUUID();
		var menhir = new MenhirDto(menhirId, 2.5, "Granite Gaulois", DecorativenessDto.DECORATED, "Test menhir");
		when(quarryApi.getAllMenhirs()).thenReturn(List.of(menhir));
		when(quarryApi.getMenhirById(menhirId)).thenReturn(menhir);

		webTestClient.get().uri("/api/menhirs").exchange()
			.expectStatus().isOk()
			.expectBodyList(MenhirDto.class).isEqualTo(List.of(menhir));
		webTestClient.get().uri("/api/menhirs/{id}", menhirId).exchange()
			.expectStatus().isOk()
			.expectBody(MenhirDto.class).isEqualTo(menhir);
		webTestClient.delete().uri("/api/quarry/{id}", menhirId).exchange()
			.expectStatus().isOk();
		verify(quarryApi).deleteById(menhirId);
	}

	@Test
	void buyMenhir() {
		var anyId = UUID.randomUUID();
		var item = new BasketDto.BasketItem("boar", 2);
		var basket = new BasketDto(List.of(item));
		when(quarryApi.getMenhirById(anyId))
			.thenReturn(new MenhirDto(anyId, 2.5, "Granite Gaulois", DecorativenessDto.DECORATED, "Test menhir"));
		when(basketApi.getBasket()).thenReturn(BasketDto.empty(), basket, BasketDto.empty());
		when(basketApi.offer(item)).thenReturn(basket);
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isBadRequest();
		verify(quarryApi, never()).deleteById(anyId);
		verify(basketApi, never()).leave();
		webTestClient.put().uri("/api/basket/offer").bodyValue(item).exchange()
			.expectStatus().isOk().expectBody(BasketDto.class).isEqualTo(basket);
		verify(basketApi).offer(item);
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isOk();
		verify(quarryApi).deleteById(anyId);
		verify(basketApi).leave();
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isBadRequest();
	}

	@Test
	void oldBasketResetEndpointStillWorks() {
		webTestClient.delete().uri("/api/basket").exchange().expectStatus().isOk();
		verify(basketApi).leave();
	}
}
