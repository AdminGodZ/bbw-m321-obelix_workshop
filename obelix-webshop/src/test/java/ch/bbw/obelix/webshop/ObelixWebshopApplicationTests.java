package ch.bbw.obelix.webshop;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
		when(quarryApi.getMenhirById(anyId))
			.thenReturn(new MenhirDto(anyId, 2.5, "Granite Gaulois", DecorativenessDto.DECORATED, "Test menhir"));
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isBadRequest();
		webTestClient.put().uri("/api/basket/offer").bodyValue(new BasketDto.BasketItem("boar", 2)).exchange().expectStatus().isOk();
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isOk();
		verify(quarryApi).deleteById(anyId);
		webTestClient.post().uri("/api/basket/buy/{id}", anyId).exchange().expectStatus().isBadRequest();
	}
}
