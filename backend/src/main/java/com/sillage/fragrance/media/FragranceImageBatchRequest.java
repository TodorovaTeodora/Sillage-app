package com.sillage.fragrance.media;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FragranceImageBatchRequest(
		@Size(max = 20) List<@Valid FragranceImageRequest> fragrances
) {
}
