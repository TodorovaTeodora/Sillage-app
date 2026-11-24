package com.sillage.fragrance.media;

import java.util.List;
import java.util.Map;

public record FragranceImageBatch(
		Map<String, MediaAsset> images,
		List<String> warnings
) {
}
