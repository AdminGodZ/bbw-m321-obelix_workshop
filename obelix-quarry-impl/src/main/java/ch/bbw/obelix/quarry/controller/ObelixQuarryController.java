package ch.bbw.obelix.quarry.controller;

import java.util.List;
import java.util.UUID;

import ch.bbw.obelix.quarry.api.QuarryApi;
import ch.bbw.obelix.quarry.dto.MenhirDto;
import ch.bbw.obelix.quarry.entity.MenhirEntity;
import ch.bbw.obelix.quarry.repository.MenhirRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ObelixQuarryController implements QuarryApi {

	private final MenhirRepository menhirRepository;

	@Override
	@GetMapping("/api/menhirs")
	public List<MenhirDto> getAllMenhirs() {
		return menhirRepository.findAll()
			.stream().map(MenhirEntity::toDto).toList();
	}

	@Override
	@GetMapping("/api/menhirs/{menhirId}")
	public MenhirDto getMenhirById(@PathVariable UUID menhirId) {
		return menhirRepository.findById(menhirId)
			.map(MenhirEntity::toDto)
			.orElseThrow(() -> new UnknownMenhirException("unknown menhir with id " + menhirId));
	}

	/**
	 * Note that this should only be called by Asterix himself. Hopefully, no customer will ever find this endpoint...
	 */
	@Override
	@DeleteMapping("/api/quarry/{menhirId}")
	public void deleteById(@PathVariable UUID menhirId) {
		menhirRepository.deleteById(menhirId);
	}

	@StandardException
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public static class UnknownMenhirException extends RuntimeException {}
}
