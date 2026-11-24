package com.sillage.fragrance.service;

import com.sillage.fragrance.provider.DemoFragranceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FragranceServiceTests {

	private FragranceService service;

	@BeforeEach
	void setUp() {
		service = new FragranceService(new DemoFragranceProvider());
	}

	@Test
	void searchesNamesAndManufacturersOnly() {
		assertThat(service.search("aventus", null, null, null, null))
				.extracting("id")
				.containsExactly("aventus");

		assertThat(service.search("chanel", null, null, null, null))
				.extracting("id")
				.containsExactly("bleu-de-chanel");

		assertThat(service.search("pineapple", null, null, null, null)).isEmpty();
		assertThat(service.search("woody", null, null, null, null)).isEmpty();
	}

	@Test
	void combinesStructuredFilters() {
		assertThat(service.search(null, List.of("Dior"), "Bergamot", 2010, 2020))
				.extracting("id")
				.containsExactly("sauvage");
	}

	@Test
	void filtersByMultipleManufacturers() {
		assertThat(service.search(null, List.of("Dior", "Creed"), null, null, null))
				.extracting("id")
				.containsExactly("aventus", "sauvage");
	}

	@Test
	void rejectsAnInvertedYearRange() {
		assertThatThrownBy(() -> service.search(null, null, null, 2020, 2010))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("fromYear");
	}
}
