package ch.bbw.obelix.basket.service;

import java.util.ArrayList;
import java.util.List;

import ch.bbw.obelix.basket.dto.BasketDto;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObelixBasketService {

	private final ObservationRegistry observationRegistry;
	private BasketDto basket = BasketDto.empty();

	public synchronized BasketDto getBasket() {
		return Observation.createNotStarted("basket.read", observationRegistry)
			.observe(() -> basket);
	}

	public synchronized BasketDto offer(BasketDto.BasketItem basketItem) {
		return Observation.createNotStarted("basket.offer", observationRegistry)
			.observe(() -> {
				var items = new ArrayList<>(basket.items());
				items.add(basketItem);
				basket = new BasketDto(List.copyOf(items));
				return basket;
			});
	}

	public synchronized void leave() {
		Observation.createNotStarted("basket.clear", observationRegistry)
			.observe(() -> {
				basket = BasketDto.empty();
			});
	}
}
