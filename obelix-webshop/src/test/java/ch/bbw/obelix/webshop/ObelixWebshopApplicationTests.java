package ch.bbw.obelix.webshop;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import ch.bbw.obelix.quarry.api.QuarryApi;
import ch.bbw.obelix.quarry.dto.DecorativenessDto;
import ch.bbw.obelix.quarry.dto.MenhirDto;
import ch.bbw.obelix.webshop.dto.BasketDto;

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

	@Test
	void buyMenhir() {
		var anyId = UUID.randomUUID();
		when(quarryApi.getMenhirById(anyId))
			.thenReturn(new MenhirDto(anyId, 2.5, "Granite Gaulois", DecorativenessDto.DECORATED, "Test menhir"));
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/api/basket/offer").bodyValue(new BasketDto.BasketItem("boar", 2)).exchange().expectStatus().isOk();
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isOk();
		verify(quarryApi).deleteById(anyId);
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isBadRequest();
	}
}
