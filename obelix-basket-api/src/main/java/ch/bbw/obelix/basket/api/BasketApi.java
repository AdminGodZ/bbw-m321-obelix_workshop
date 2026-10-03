package ch.bbw.obelix.basket.api;

import ch.bbw.obelix.basket.dto.BasketDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface BasketApi {

	@GetExchange("/api/basket")
	BasketDto getBasket();

	@PutExchange("/api/basket/offer")
	BasketDto offer(@RequestBody BasketDto.BasketItem basketItem);

	@DeleteExchange("/api/basket")
	void leave();
}
