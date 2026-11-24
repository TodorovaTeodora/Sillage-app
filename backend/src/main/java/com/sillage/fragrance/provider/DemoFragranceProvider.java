package com.sillage.fragrance.provider;

import com.sillage.fragrance.model.Fragrance;
import com.sillage.fragrance.model.FragranceFacets;
import com.sillage.fragrance.model.NotePyramid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "fragrance.provider", havingValue = "demo")
public class DemoFragranceProvider implements FragranceProvider {

	private final List<Fragrance> fragrances = List.of(
			fragrance(
					"aventus", "Aventus", "Creed", 2010, "Fruity Chypre", "Eau de Parfum",
					"A bright, smoky composition balancing fruit, woods, and moss.",
					List.of("Pineapple", "Bergamot", "Blackcurrant"),
					List.of("Birch", "Jasmine", "Rose"),
					List.of("Musk", "Oakmoss", "Vanilla")
			),
			fragrance(
					"baccarat-rouge-540", "Baccarat Rouge 540", "Maison Francis Kurkdjian", 2015,
					"Amber Floral", "Eau de Parfum",
					"An airy amber composition with mineral warmth and a lasting woody trail.",
					List.of("Saffron", "Jasmine"),
					List.of("Amberwood", "Ambergris"),
					List.of("Fir Resin", "Cedar")
			),
			fragrance(
					"black-orchid", "Black Orchid", "Tom Ford", 2006, "Amber Floral", "Eau de Parfum",
					"A dark floral fragrance layered with spice, fruit, and earthy woods.",
					List.of("Truffle", "Bergamot", "Blackcurrant"),
					List.of("Black Orchid", "Spices", "Lotus"),
					List.of("Patchouli", "Vanilla", "Sandalwood")
			),
			fragrance(
					"bleu-de-chanel", "Bleu de Chanel", "Chanel", 2010, "Woody Aromatic", "Eau de Toilette",
					"A crisp citrus opening that settles into incense and polished woods.",
					List.of("Grapefruit", "Lemon", "Mint"),
					List.of("Ginger", "Nutmeg", "Jasmine"),
					List.of("Incense", "Cedar", "Sandalwood")
			),
			fragrance(
					"acqua-di-gio", "Acqua di Gio", "Giorgio Armani", 1996, "Aquatic Aromatic", "Eau de Toilette",
					"A transparent marine scent with citrus, aromatic herbs, and clean woods.",
					List.of("Lime", "Bergamot", "Neroli"),
					List.of("Marine Notes", "Jasmine", "Rosemary"),
					List.of("White Musk", "Cedar", "Patchouli")
			),
			fragrance(
					"la-vie-est-belle", "La Vie Est Belle", "Lancome", 2012, "Floral Gourmand", "Eau de Parfum",
					"A sweet iris-led floral with a praline heart and soft vanilla depth.",
					List.of("Blackcurrant", "Pear"),
					List.of("Iris", "Jasmine", "Orange Blossom"),
					List.of("Praline", "Vanilla", "Patchouli")
			),
			fragrance(
					"light-blue", "Light Blue", "Dolce & Gabbana", 2001, "Citrus Floral", "Eau de Toilette",
					"A sunlit Mediterranean blend of sparkling fruit, flowers, and pale woods.",
					List.of("Sicilian Lemon", "Apple", "Cedar"),
					List.of("Bamboo", "Jasmine", "White Rose"),
					List.of("Cedar", "Musk", "Amber")
			),
			fragrance(
					"libre", "Libre", "Yves Saint Laurent", 2019, "Floral Aromatic", "Eau de Parfum",
					"A contrast of aromatic lavender, orange blossom, and warm vanilla.",
					List.of("Lavender", "Mandarin", "Blackcurrant"),
					List.of("Orange Blossom", "Jasmine"),
					List.of("Vanilla", "Cedar", "Musk")
			),
			fragrance(
					"sauvage", "Sauvage", "Dior", 2015, "Aromatic Fougere", "Eau de Toilette",
					"A fresh, spicy aromatic built around bergamot and dry amber woods.",
					List.of("Bergamot", "Pepper"),
					List.of("Lavender", "Pink Pepper", "Patchouli"),
					List.of("Ambroxan", "Cedar", "Labdanum")
			),
			fragrance(
					"shalimar", "Shalimar", "Guerlain", 1925, "Amber Spicy", "Eau de Parfum",
					"A landmark amber fragrance with citrus, powdery florals, and vanilla.",
					List.of("Bergamot", "Lemon"),
					List.of("Iris", "Jasmine", "Rose"),
					List.of("Vanilla", "Tonka Bean", "Opoponax")
			),
			fragrance(
					"terre-d-hermes", "Terre d'Hermes", "Hermes", 2006, "Woody Spicy", "Eau de Toilette",
					"A mineral woody fragrance pairing vibrant citrus with vetiver and cedar.",
					List.of("Orange", "Grapefruit"),
					List.of("Pepper", "Pelargonium"),
					List.of("Vetiver", "Cedar", "Benzoin")
			),
			fragrance(
					"wood-sage-sea-salt", "Wood Sage & Sea Salt", "Jo Malone London", 2014,
					"Aromatic", "Cologne",
					"A breezy mineral fragrance inspired by salt air and windswept coastal herbs.",
					List.of("Ambrette Seed"),
					List.of("Sea Salt"),
					List.of("Sage")
			)
	);

	@Override
	public List<Fragrance> search(String query) {
		String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		return fragrances.stream()
				.filter(fragrance -> normalizedQuery.isBlank()
						|| fragrance.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
						|| fragrance.manufacturer().toLowerCase(Locale.ROOT).contains(normalizedQuery))
				.toList();
	}

	@Override
	public Optional<Fragrance> findById(String id) {
		return fragrances.stream()
				.filter(fragrance -> fragrance.id().equals(id))
				.findFirst();
	}

	@Override
	public FragranceFacets facets() {
		int manufacturerCount = (int) fragrances.stream()
				.map(Fragrance::manufacturer)
				.distinct()
				.count();
		int noteCount = (int) fragrances.stream()
				.flatMap(fragrance -> fragrance.pyramid().allNotes().stream())
				.distinct()
				.count();
		int earliestYear = fragrances.stream()
				.map(Fragrance::year)
				.filter(java.util.Objects::nonNull)
				.min(Comparator.naturalOrder())
				.orElse(0);
		int latestYear = fragrances.stream()
				.map(Fragrance::year)
				.filter(java.util.Objects::nonNull)
				.max(Comparator.naturalOrder())
				.orElse(0);
		return new FragranceFacets(
				manufacturerCount,
				noteCount,
				earliestYear,
				latestYear,
				fragrances.size(),
				fragrances.stream()
						.map(Fragrance::manufacturer)
						.distinct()
						.sorted(String.CASE_INSENSITIVE_ORDER)
						.toList()
		);
	}

	private static Fragrance fragrance(
			String id,
			String name,
			String manufacturer,
			int year,
			String family,
			String concentration,
			String description,
			List<String> top,
			List<String> heart,
			List<String> base
	) {
		return new Fragrance(
				id,
				name,
				manufacturer,
				year,
				family,
				concentration,
				description,
				new NotePyramid(top, heart, base)
		);
	}
}
