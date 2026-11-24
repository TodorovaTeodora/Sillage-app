package com.sillage.fragrance.model;

public record Fragrance(
		String id,
		String name,
		String manufacturer,
		Integer year,
		String family,
		String concentration,
		String description,
		NotePyramid pyramid
) {
}
