package ch.bbw.obelix.webshop.service;

import java.util.Locale;
import java.util.UUID;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import ch.bbw.obelix.basket.api.BasketApi;
import ch.bbw.obelix.quarry.api.QuarryApi;
import ch.bbw.obelix.quarry.dto.DecorativenessDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Note that Obelix is definitely not multitasking-capable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObelixWebshopService {

	private final QuarryApi quarryWebclient;
	private final BasketApi basketApi;
	private final ObservationRegistry observationRegistry;

	public boolean isGoodOffer(DecorativenessDto decorativeness) {
		var basket = basketApi.getBasket();
		var stoneWorth = decorativeness.ordinal();
		var basketWorth = basket.items()
			.stream().map(x -> switch (x.name().toLowerCase(Locale.ROOT)) {
				case "boar" -> 5; // oh boy, oh boy!
				case "honey" -> 2;
				case "magic potion" -> 0; // not allowed to drink this!
				default -> 1; // everything is worth something
			} * x.count()).reduce(0, Integer::sum);
		log.info("basket worth {} vs menhir worth {} ({})", basketWorth, decorativeness, stoneWorth);
		return basketWorth >= stoneWorth;
	}

	public void exchange(UUID menhirId) {
		Observation.createNotStarted("webshop.purchase", observationRegistry)
			.highCardinalityKeyValue("menhir.id", menhirId.toString())
			.observe(() -> {
				var menhir = quarryWebclient.getMenhirById(menhirId);
				var decorativeness = menhir.decorativeness();
				if (!isGoodOffer(decorativeness)) {
					throw new BadOfferException("Bad Offer: That won't even feed Idefix!");
				}
				quarryWebclient.deleteById(menhirId);
				basketApi.leave();
			});
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class BadOfferException extends RuntimeException {
		public BadOfferException(String message) {
			super(message);
		}
	}
}
