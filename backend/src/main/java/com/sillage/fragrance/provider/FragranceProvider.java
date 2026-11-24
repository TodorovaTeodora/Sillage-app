package com.sillage.fragrance.provider;

import com.sillage.fragrance.model.Fragrance;
import com.sillage.fragrance.model.FragranceFacets;

import java.util.List;
import java.util.Optional;

public interface FragranceProvider {
	List<Fragrance> search(String query);

	Optional<Fragrance> findById(String id);

	FragranceFacets facets();
}
