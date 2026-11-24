package com.sillage.fragrance.media;

public record MediaAsset(
		String imageUrl,
		String sourcePageUrl,
		String author,
		String licenseName,
		String licenseUrl,
		String alt
) {
}
