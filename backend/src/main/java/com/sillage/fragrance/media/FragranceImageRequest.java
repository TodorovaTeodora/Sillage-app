package com.sillage.fragrance.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FragranceImageRequest(
		@NotBlank @Size(max = 160) String id,
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 120) String manufacturer
) {
}
