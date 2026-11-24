package com.sillage.fragrance.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FragranceMediaRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 120) String manufacturer,
		@Size(max = 24) List<@NotBlank @Size(max = 80) String> notes
) {
}
