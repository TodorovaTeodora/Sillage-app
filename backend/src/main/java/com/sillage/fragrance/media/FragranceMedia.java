package com.sillage.fragrance.media;

import java.util.Map;
import java.util.List;

public record FragranceMedia(
		MediaAsset fragranceImage,
		Map<String, MediaAsset> noteImages,
		List<String> warnings
) {
}
