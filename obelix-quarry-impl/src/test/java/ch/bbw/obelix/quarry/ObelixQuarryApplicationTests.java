package ch.bbw.obelix.quarry;

import static org.assertj.core.api.Assertions.assertThat;

import ch.bbw.obelix.quarry.dto.MenhirDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ObelixQuarryApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void listFindAndDeleteMenhir() {
		var menhirs = webTestClient.get().uri("/api/menhirs")
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MenhirDto.class).hasSize(3)
			.returnResult().getResponseBody();
		assertThat(menhirs).isNotNull();
		var menhir = menhirs.getFirst();

		webTestClient.get().uri("/api/menhirs/{id}", menhir.id())
			.exchange()
			.expectStatus().isOk()
			.expectBody(MenhirDto.class).isEqualTo(menhir);

		webTestClient.delete().uri("/api/quarry/{id}", menhir.id())
			.exchange()
			.expectStatus().isOk();

		webTestClient.get().uri("/api/menhirs/{id}", menhir.id())
			.exchange()
			.expectStatus().isBadRequest();

		webTestClient.get().uri("/api/menhirs")
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MenhirDto.class).hasSize(2)
			.value(remaining -> assertThat(remaining)
				.extracting(MenhirDto::id).doesNotContain(menhir.id()));
	}
}
