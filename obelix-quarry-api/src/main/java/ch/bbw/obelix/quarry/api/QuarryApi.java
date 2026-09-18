package ch.bbw.obelix.quarry.api;

import java.util.List;
import java.util.UUID;

import ch.bbw.obelix.quarry.dto.MenhirDto;

public interface QuarryApi {

	List<MenhirDto> getAllMenhirs();

	MenhirDto getMenhirById(UUID menhirId);

	void deleteById(UUID menhirId);
}
