package com.sillage.fragrance.service;

import com.sillage.fragrance.model.Fragrance;
import com.sillage.fragrance.model.FragranceFacets;
import com.sillage.fragrance.provider.FragranceProvider;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FragranceService {

	private final FragranceProvider provider;

	public FragranceService(FragranceProvider provider) {
		this.provider = provider;
	}

	public List<Fragrance> search(
			String query,
			List<String> manufacturers,
			String note,
			Integer fromYear,
			Integer toYear
	) {
		if (fromYear != null && toYear != null && fromYear > toYear) {
			throw new IllegalArgumentException("fromYear must be less than or equal to toYear");
		}

		String normalizedQuery = normalize(query);
		List<String> selectedManufacturers = manufacturers == null
				? List.of()
				: manufacturers.stream()
						.filter(value -> value != null && !value.isBlank())
						.map(String::trim)
						.distinct()
						.toList();
		Set<String> normalizedManufacturers = selectedManufacturers.stream()
				.map(FragranceService::normalize)
				.collect(Collectors.toUnmodifiableSet());
		String normalizedNote = normalize(note);

		return loadCandidates(query, normalizedQuery, selectedManufacturers).stream()
				.filter(fragrance -> matchesQuery(fragrance, normalizedQuery))
				.filter(fragrance -> normalizedManufacturers.isEmpty()
						|| normalizedManufacturers.contains(normalize(fragrance.manufacturer())))
				.filter(fragrance -> normalizedNote.isBlank()
						|| fragrance.pyramid().allNotes().stream()
						.map(FragranceService::normalize)
						.anyMatch(value -> value.equals(normalizedNote)))
				.filter(fragrance -> fromYear == null
						|| fragrance.year() != null && fragrance.year() >= fromYear)
				.filter(fragrance -> toYear == null
						|| fragrance.year() != null && fragrance.year() <= toYear)
				.sorted(java.util.Comparator.comparing(Fragrance::name))
				.toList();
	}

	private List<Fragrance> loadCandidates(
			String query,
			String normalizedQuery,
			List<String> manufacturers
	) {
		if (!normalizedQuery.isBlank() || manufacturers.isEmpty()) {
			return provider.search(query);
		}

		Map<String, Fragrance> fragrancesById = new LinkedHashMap<>();
		manufacturers.forEach(manufacturer ->
				provider.search(manufacturer).forEach(fragrance ->
						fragrancesById.putIfAbsent(fragrance.id(), fragrance)));
		return List.copyOf(fragrancesById.values());
	}

	public Fragrance findById(String id) {
		return provider.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Fragrance not found: " + id));
	}

	public FragranceFacets facets() {
		return provider.facets();
	}

	private static boolean matchesQuery(Fragrance fragrance, String query) {
		if (query.isBlank()) {
			return true;
		}

		return normalize(fragrance.name()).contains(query)
				|| normalize(fragrance.manufacturer()).contains(query);
	}

	private static String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}
}
