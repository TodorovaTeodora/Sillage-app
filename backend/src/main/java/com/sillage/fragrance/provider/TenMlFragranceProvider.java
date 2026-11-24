package com.sillage.fragrance.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sillage.fragrance.model.Fragrance;
import com.sillage.fragrance.model.FragranceFacets;
import com.sillage.fragrance.model.NotePyramid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "fragrance.provider", havingValue = "10ml", matchIfMissing = true)
public class TenMlFragranceProvider implements FragranceProvider {

	private static final int SEARCH_LIMIT = 20;
	private static final int NOTE_COUNT = 285;
	private static final int EARLIEST_YEAR = 1700;
	private static final Map<String, Map<String, String>> NOTE_CORRECTIONS = Map.of(
			"amouage-guidance-46-extrait",
			Map.of("Incense", "Frankincense")
	);

	private final RestClient restClient;
	private volatile FragranceFacets cachedFacets;

	public TenMlFragranceProvider(@Qualifier("tenMlRestClient") RestClient tenMlRestClient) {
		this.restClient = tenMlRestClient;
	}

	@Override
	public List<Fragrance> search(String query) {
		try {
			TenMlSearchResponse response = restClient.get()
					.uri(uriBuilder -> {
						var builder = uriBuilder
								.path("/api/v1/perfumes")
								.queryParam("limit", SEARCH_LIMIT)
								.queryParam("format", "detailed");
						if (query != null && !query.isBlank()) {
							builder.queryParam("q", query.trim());
						}
						return builder.build();
					})
					.retrieve()
					.body(TenMlSearchResponse.class);

			if (response == null || response.results() == null) {
				throw new FragranceProviderException("10ML returned an incomplete search response", null);
			}

			return response.results().stream()
					.map(this::mapFragrance)
					.toList();
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				return List.of();
			}
			throw unavailable(exception);
		} catch (RestClientException exception) {
			throw unavailable(exception);
		}
	}

	@Override
	public Optional<Fragrance> findById(String id) {
		try {
			TenMlLookupResponse response = restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/api/v1/perfumes/{id}")
							.queryParam("format", "detailed")
							.build(id))
					.retrieve()
					.body(TenMlLookupResponse.class);

			if (response == null || !response.found() || response.perfume() == null) {
				return Optional.empty();
			}
			if (!response.exact() && !id.equals(response.perfume().id())) {
				return Optional.empty();
			}
			return Optional.of(mapFragrance(response.perfume()));
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				return Optional.empty();
			}
			throw unavailable(exception);
		} catch (RestClientException exception) {
			throw unavailable(exception);
		}
	}

	@Override
	public FragranceFacets facets() {
		FragranceFacets existing = cachedFacets;
		if (existing != null) {
			return existing;
		}

		try {
			TenMlSearchResponse perfumes = restClient.get()
					.uri("/api/v1/perfumes?limit=1")
					.retrieve()
					.body(TenMlSearchResponse.class);
			List<String> manufacturers = new ArrayList<>();
			Set<String> cursors = new HashSet<>();
			String cursor = null;
			int manufacturerCount = 0;
			do {
				String pageCursor = cursor;
				TenMlHouseResponse houses = restClient.get()
						.uri(uriBuilder -> {
							var builder = uriBuilder
									.path("/api/v1/houses")
									.queryParam("limit", 50);
							if (pageCursor != null && !pageCursor.isBlank()) {
								builder.queryParam("cursor", pageCursor);
							}
							return builder.build();
						})
						.retrieve()
						.body(TenMlHouseResponse.class);
				if (houses == null || houses.houses() == null) {
					throw new FragranceProviderException("10ML returned incomplete catalog statistics", null);
				}
				manufacturerCount = houses.total();
				houses.houses().stream()
						.map(TenMlHouse::name)
						.filter(name -> name != null && !name.isBlank())
						.forEach(manufacturers::add);
				cursor = houses.nextCursor();
				if (cursor != null && !cursor.isBlank() && !cursors.add(cursor)) {
					throw new FragranceProviderException("10ML returned a repeated houses cursor", null);
				}
			} while (cursor != null && !cursor.isBlank());

			if (perfumes == null) {
				throw new FragranceProviderException("10ML returned incomplete catalog statistics", null);
			}

			FragranceFacets facets = new FragranceFacets(
					manufacturerCount,
					NOTE_COUNT,
					EARLIEST_YEAR,
					Year.now().getValue(),
					perfumes.total(),
					manufacturers.stream()
							.distinct()
							.sorted(String.CASE_INSENSITIVE_ORDER)
							.toList()
			);
			cachedFacets = facets;
			return facets;
		} catch (RestClientException exception) {
			throw unavailable(exception);
		}
	}

	private Fragrance mapFragrance(TenMlPerfume perfume) {
		TenMlPyramid sourcePyramid = perfume.pyramid();
		NotePyramid pyramid = new NotePyramid(
				displayNotes(perfume.id(), sourcePyramid == null ? List.of() : sourcePyramid.top()),
				displayNotes(perfume.id(), sourcePyramid == null ? List.of() : sourcePyramid.heart()),
				displayNotes(perfume.id(), sourcePyramid == null ? List.of() : sourcePyramid.base())
		);

		return new Fragrance(
				perfume.id(),
				perfume.name(),
				perfume.house(),
				perfume.year(),
				perfume.family(),
				displayConcentration(perfume.concentration()),
				perfume.character(),
				pyramid
		);
	}

	private static List<String> displayNotes(String fragranceId, List<String> notes) {
		if (notes == null) {
			return List.of();
		}
		Map<String, String> corrections = NOTE_CORRECTIONS.getOrDefault(fragranceId, Map.of());
		return notes.stream()
				.map(TenMlFragranceProvider::capitalize)
				.map(note -> corrections.getOrDefault(note, note))
				.toList();
	}

	private static String capitalize(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
	}

	private static String displayConcentration(String concentration) {
		if (concentration == null || concentration.isBlank()) {
			return "Not listed";
		}
		return switch (concentration.toUpperCase(Locale.ROOT)) {
			case "EDP" -> "Eau de Parfum";
			case "EDT" -> "Eau de Toilette";
			case "EDC" -> "Eau de Cologne";
			default -> concentration;
		};
	}

	private static FragranceProviderException unavailable(Exception cause) {
		return new FragranceProviderException("10ML Fragrance Index is unavailable", cause);
	}

	record TenMlSearchResponse(int total, List<TenMlPerfume> results) {
	}

	record TenMlHouseResponse(
			int total,
			List<TenMlHouse> houses,
			@JsonProperty("next_cursor") String nextCursor
	) {
	}

	record TenMlHouse(String name) {
	}

	record TenMlLookupResponse(boolean found, boolean exact, TenMlPerfume perfume) {
	}

	record TenMlPerfume(
			String id,
			String name,
			String house,
			Integer year,
			String family,
			String character,
			String concentration,
			@JsonProperty("key_notes") List<String> keyNotes,
			TenMlPyramid pyramid
	) {
	}

	record TenMlPyramid(List<String> top, List<String> heart, List<String> base) {
	}
}
