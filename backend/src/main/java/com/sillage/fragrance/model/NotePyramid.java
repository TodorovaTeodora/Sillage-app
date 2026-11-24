package com.sillage.fragrance.model;

import java.util.List;

public record NotePyramid(
		List<String> top,
		List<String> heart,
		List<String> base
) {
	public List<String> allNotes() {
		return java.util.stream.Stream.of(top, heart, base)
				.flatMap(List::stream)
				.distinct()
				.toList();
	}
}
