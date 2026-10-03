package ch.bbw.obelix.basket.controller;

import ch.bbw.obelix.basket.api.BasketApi;
import ch.bbw.obelix.basket.dto.BasketDto;
import ch.bbw.obelix.basket.service.ObelixBasketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ObelixBasketController implements BasketApi {

	private final ObelixBasketService basketService;

	@Override
	public BasketDto getBasket() {
		return basketService.getBasket();
	}

	@Override
	public BasketDto offer(BasketDto.BasketItem basketItem) {
		return basketService.offer(basketItem);
	}

	@Override
	public void leave() {
		basketService.leave();
	}
}
