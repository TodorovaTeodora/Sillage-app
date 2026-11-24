package com.sillage.fragrance.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CommonsMediaService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommonsMediaService.class);
	private static final int MAX_CACHE_ENTRIES = 1_000;
	private static final Map<String, NoteSearch> NOTE_SEARCHES = Map.ofEntries(
			Map.entry("bergamot", new NoteSearch(
					"Citrus bergamia fruit",
					List.of("bergamot", "bergamia", "bergamotte")
			)),
			Map.entry("incense", new NoteSearch(
					"frankincense resin",
					List.of("incense", "frankincense", "olibanum")
			)),
			Map.entry("frankincense", new NoteSearch(
					"frankincense resin",
					List.of("frankincense", "olibanum")
			)),
			Map.entry("cinnamon", new NoteSearch(
					"cinnamon spice",
					List.of("cinnamon", "cinnamomum", "canelle")
			)),
			Map.entry("pink pepper", new NoteSearch(
					"Schinus pink pepper berries",
					List.of("schinus", "pink pepper")
			)),
			Map.entry("marine notes", new NoteSearch(
					"ocean sea waves",
					List.of("ocean", "sea", "wave")
			)),
			Map.entry("musk", new NoteSearch(
					"musk mallow flower",
					List.of("musk", "abelmoschus")
			)),
			Map.entry("white musk", new NoteSearch(
					"musk mallow flower",
					List.of("musk", "abelmoschus")
			)),
			Map.entry("oakmoss", new NoteSearch(
					"Evernia prunastri lichen",
					List.of("oakmoss", "evernia", "prunastri")
			)),
			Map.entry("neroli", new NoteSearch(
					"neroli orange blossom",
					List.of("neroli", "orange blossom")
			)),
			Map.entry("vanilla", new NoteSearch(
					"vanilla beans pods",
					List.of("vanilla 6beans", "vanilla beans")
			)),
			Map.entry("cedar", new NoteSearch(
					"Cedrus cedar tree wood",
					List.of("cedrus", "cedar")
			)),
			Map.entry("cedarwood", new NoteSearch(
					"Cedrus libani wood",
					List.of("cedrus wood")
			)),
			Map.entry("cedar wood", new NoteSearch(
					"Cedrus libani wood",
					List.of("cedrus wood")
			)),
			Map.entry("juniper berry", new NoteSearch(
					"Juniperus communis berries",
					List.of("juniperus communis")
			)),
			Map.entry("juniper berries", new NoteSearch(
					"Juniperus communis berries",
					List.of("juniperus communis")
			)),
			Map.entry("sandalwood", new NoteSearch(
					"Santalum album sandalwood",
					List.of("santalum", "sandalwood")
			)),
			Map.entry("patchouli", new NoteSearch(
					"Pogostemon cablin patchouli plant",
					List.of("pogostemon", "patchouli")
			)),
			Map.entry("vetiver", new NoteSearch(
					"Chrysopogon zizanioides vetiver plant",
					List.of("chrysopogon", "zizanioides", "vetiver")
			)),
			Map.entry("jasmine", new NoteSearch(
					"Jasminum jasmine flower",
					List.of("jasminum", "jasmine")
			)),
			Map.entry("rose", new NoteSearch(
					"rose flower",
					List.of("rose")
			)),
			Map.entry("lavender", new NoteSearch(
					"Lavandula lavender flower",
					List.of("lavandula", "lavender")
			)),
			Map.entry("cardamom", new NoteSearch(
					"cardamom spice pods",
					List.of("cardamom")
			)),
			Map.entry("black pepper", new NoteSearch(
					"Piper nigrum black pepper",
					List.of("piper nigrum", "black pepper")
			)),
			Map.entry("lemon", new NoteSearch(
					"lemon citrus fruit",
					List.of("lemon")
			)),
			Map.entry("grapefruit", new NoteSearch(
					"grapefruit citrus fruit",
					List.of("grapefruit")
			))
	);
	private static final List<String> IRRELEVANT_NOTE_TITLE_TOKENS = List.of(
			"waxwing",
			"cake",
			"ice cream",
			"restaurant",
			"street",
			"portrait",
			"logo",
			"perfume bottle",
			"candle",
			"soap"
	);

	private final RestClient restClient;
	private final AwinImageService awinImageService;
	private final Object requestLock = new Object();
	private long nextRequestAt;
	private final Map<String, Optional<MediaAsset>> cache = Collections.synchronizedMap(
			new LinkedHashMap<>(128, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, Optional<MediaAsset>> eldest) {
					return size() > MAX_CACHE_ENTRIES;
				}
			}
	);

	public CommonsMediaService(
			@Qualifier("commonsRestClient") RestClient commonsRestClient,
			AwinImageService awinImageService
	) {
		this.restClient = commonsRestClient;
		this.awinImageService = awinImageService;
	}

	public FragranceMedia findMedia(FragranceMediaRequest request) {
		List<String> warnings = new ArrayList<>();
		MediaAsset fragranceImage = null;
		try {
			fragranceImage = findFragranceImage(request.name(), request.manufacturer())
					.orElse(null);
		} catch (MediaProviderException exception) {
			warnings.add(exception.getMessage());
		}
		Map<String, MediaAsset> noteImages = new LinkedHashMap<>();
		Set<String> uniqueNotes = new LinkedHashSet<>(
				request.notes() == null ? List.of() : request.notes()
		);

		for (String note : uniqueNotes) {
			try {
				findNoteImage(note).ifPresent(image -> noteImages.put(note, image));
			} catch (MediaProviderException exception) {
				warnings.add("Image unavailable for " + note);
			}
		}

		return new FragranceMedia(fragranceImage, noteImages, warnings.stream().distinct().toList());
	}

	public FragranceImageBatch findFragranceImages(FragranceImageBatchRequest request) {
		Map<String, MediaAsset> images = new LinkedHashMap<>();
		List<String> warnings = new ArrayList<>();
		if (request.fragrances() == null) {
			return new FragranceImageBatch(images, warnings);
		}

		for (FragranceImageRequest fragrance : request.fragrances()) {
			try {
				findFragranceImage(fragrance.name(), fragrance.manufacturer())
						.ifPresent(image -> images.put(fragrance.id(), image));
			} catch (MediaProviderException exception) {
				warnings.add("Image unavailable for " + fragrance.name());
			}
		}
		return new FragranceImageBatch(images, warnings);
	}

	private Optional<MediaAsset> findFragranceImage(String name, String manufacturer) {
		try {
			Optional<MediaAsset> awinImage = awinImageService.findImage(name, manufacturer);
			if (awinImage.isPresent()) {
				return awinImage;
			}
		} catch (MediaProviderException exception) {
			LOGGER.warn("Awin image lookup failed; falling back to Commons", exception);
		}

		String key = "fragrance:" + normalize(manufacturer + " " + name);
		return cached(key, () -> search(
				manufacturer + " " + name + " perfume bottle",
				800,
				3,
				page -> matchesFragrance(page.title(), name, manufacturer),
				manufacturer + " " + name + " perfume bottle"
		));
	}

	private Optional<MediaAsset> findNoteImage(String note) {
		String key = "note:" + normalize(note);
		NoteSearch noteSearch = NOTE_SEARCHES.getOrDefault(
				normalize(note),
				new NoteSearch(
						"intitle:\"" + note + "\"",
						List.of(normalize(note))
				)
		);
		return cached(key, () -> search(
				noteSearch.query(),
				360,
				8,
				page -> noteSearch.titleTokens().stream()
						.anyMatch(normalize(page.title())::contains)
						&& isRelevantNoteTitle(page.title()),
				note + " fragrance note"
		));
	}

	private Optional<MediaAsset> search(
			String query,
			int width,
			int resultLimit,
			java.util.function.Predicate<CommonsPage> filter,
			String alt
	) {
		try {
			CommonsResponse response;
			synchronized (requestLock) {
				waitForRequestSlot();
				try {
					response = restClient.get()
							.uri(uriBuilder -> uriBuilder
									.path("/w/api.php")
									.queryParam("action", "query")
									.queryParam("generator", "search")
									.queryParam("gsrsearch", query)
									.queryParam("gsrnamespace", 6)
									.queryParam("gsrlimit", resultLimit)
									.queryParam("prop", "imageinfo")
									.queryParam("iiprop", "url|extmetadata")
									.queryParam(
											"iiextmetadatafilter",
											"Artist|LicenseShortName|LicenseUrl"
									)
									.queryParam("iiurlwidth", width)
									.queryParam("format", "json")
									.queryParam("formatversion", 2)
									.build())
							.retrieve()
							.body(CommonsResponse.class);
				} finally {
					nextRequestAt = System.currentTimeMillis() + 400;
				}
			}

			if (response == null || response.query() == null || response.query().pages() == null) {
				return Optional.empty();
			}

			return response.query().pages().stream()
					.filter(filter)
					.sorted(Comparator.comparingInt(CommonsPage::index))
					.map(page -> toMediaAsset(page, alt))
					.flatMap(Optional::stream)
					.findFirst();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new MediaProviderException("Wikimedia Commons image lookup was interrupted", exception);
		} catch (RestClientException exception) {
			throw new MediaProviderException("Wikimedia Commons images are unavailable", exception);
		}
	}

	private void waitForRequestSlot() throws InterruptedException {
		long waitMillis = nextRequestAt - System.currentTimeMillis();
		if (waitMillis > 0) {
			Thread.sleep(waitMillis);
		}
	}

	private Optional<MediaAsset> toMediaAsset(CommonsPage page, String alt) {
		if (page.imageInfo() == null || page.imageInfo().isEmpty()) {
			return Optional.empty();
		}

		CommonsImageInfo image = page.imageInfo().getFirst();
		if (!isTrustedImageUrl(image.thumbnailUrl())
				|| image.descriptionUrl() == null
				|| !image.descriptionUrl().startsWith("https://commons.wikimedia.org/")) {
			return Optional.empty();
		}

		Map<String, CommonsMetadataValue> metadata = image.extMetadata() == null
				? Map.of()
				: image.extMetadata();
		String licenseName = metadataValue(metadata, "LicenseShortName", "Wikimedia Commons");
		String licenseUrl = metadataValue(metadata, "LicenseUrl", image.descriptionUrl());
		String author = cleanHtml(metadataValue(
				metadata,
				"Artist",
				"Wikimedia Commons contributor"
		));

		return Optional.of(new MediaAsset(
				image.thumbnailUrl(),
				image.descriptionUrl(),
				truncate(author, 120),
				cleanHtml(licenseName),
				licenseUrl,
				alt
		));
	}

	private Optional<MediaAsset> cached(
			String key,
			java.util.function.Supplier<Optional<MediaAsset>> loader
	) {
		Optional<MediaAsset> existing;
		synchronized (cache) {
			existing = cache.get(key);
		}
		if (existing != null) {
			return existing;
		}

		Optional<MediaAsset> loaded = loader.get();
		synchronized (cache) {
			cache.put(key, loaded);
		}
		return loaded;
	}

	private static boolean matchesFragrance(String title, String name, String manufacturer) {
		String normalizedTitle = normalize(title);
		boolean productContext = normalizedTitle.contains("perfume")
				|| normalizedTitle.contains("parfum")
				|| normalizedTitle.contains("fragrance")
				|| normalizedTitle.contains("bottle");
		return containsSignificantToken(normalizedTitle, name)
				&& (containsSignificantToken(normalizedTitle, manufacturer) || productContext);
	}

	private static boolean containsSignificantToken(String value, String source) {
		return tokens(source).stream().anyMatch(value::contains);
	}

	private static boolean isRelevantNoteTitle(String title) {
		String normalizedTitle = normalize(title);
		return IRRELEVANT_NOTE_TITLE_TOKENS.stream()
				.noneMatch(normalizedTitle::contains);
	}

	private static List<String> tokens(String value) {
		List<String> result = new ArrayList<>();
		for (String token : normalize(value).split("\\s+")) {
			if (token.length() >= 3) {
				result.add(token);
			}
		}
		return result;
	}

	private static boolean isTrustedImageUrl(String url) {
		return url != null && (
				url.startsWith("https://upload.wikimedia.org/")
						|| url.startsWith("https://thumb.wikimedia.org/")
		);
	}

	private static String metadataValue(
			Map<String, CommonsMetadataValue> metadata,
			String key,
			String fallback
	) {
		CommonsMetadataValue value = metadata.get(key);
		return value == null || value.value() == null || value.value().isBlank()
				? fallback
				: value.value();
	}

	private static String cleanHtml(String value) {
		String withoutTags = value.replaceAll("<[^>]+>", " ");
		return HtmlUtils.htmlUnescape(withoutTags)
				.replaceAll("\\s+", " ")
				.trim();
	}

	private static String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength - 1).trim() + "...";
	}

	private static String normalize(String value) {
		return value == null
				? ""
				: value.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{L}\\p{N}]+", " ")
				.trim();
	}

	record CommonsResponse(CommonsQuery query) {
	}

	record CommonsQuery(List<CommonsPage> pages) {
	}

	record CommonsPage(
			int index,
			String title,
			@JsonProperty("imageinfo") List<CommonsImageInfo> imageInfo
	) {
	}

	record CommonsImageInfo(
			@JsonProperty("thumburl") String thumbnailUrl,
			@JsonProperty("descriptionurl") String descriptionUrl,
			@JsonProperty("extmetadata") Map<String, CommonsMetadataValue> extMetadata
	) {
	}

	record CommonsMetadataValue(String value) {
	}

	record NoteSearch(String query, List<String> titleTokens) {
	}
}
