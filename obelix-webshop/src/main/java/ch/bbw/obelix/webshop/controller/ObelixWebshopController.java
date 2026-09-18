package ch.bbw.obelix.webshop.controller;

import java.util.UUID;

import ch.bbw.obelix.webshop.dto.BasketDto;
import ch.bbw.obelix.webshop.service.ObelixWebshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ObelixWebshopController {

	private final ObelixWebshopService obelixWebshopService;

	@GetMapping("/api")
	public String welcome() {
		return "Welcome to Obelix's Menhir Shop! The finest menhirs in all of Gaul! Ces Romains sont fous!";
	}

	@PutMapping("/api/basket/offer")
	public BasketDto offer(@RequestBody BasketDto.BasketItem basketItem) {
		return obelixWebshopService.offer(basketItem);
	}

	@DeleteMapping("/api/basket")
	public void leave() {
		obelixWebshopService.leave();
	}

	/**
	 * Decide if the current basket is worthy enough for a beautiful menhir.
	 *
	 * @param menhirId the menhir to buy
	 * @throws ObelixWebshopService.BadOfferException in case the basket is tiny
	 */
	@PostMapping("/api/basket/buy/{menhirId}")
	public void exchangeFor(@PathVariable UUID menhirId) {
		obelixWebshopService.exchange(menhirId);
	}
}
