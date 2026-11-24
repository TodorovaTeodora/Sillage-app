package com.sillage.fragrance.model;

import java.util.List;

public record FragranceFacets(
		int manufacturerCount,
		int noteCount,
		int earliestYear,
		int latestYear,
		int totalCount,
		List<String> manufacturers
) {
}
