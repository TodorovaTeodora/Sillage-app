package com.sillage.fragrance.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AwinImageService {

	private static final Duration REFRESH_INTERVAL = Duration.ofHours(6);
	private static final String LICENSE_URL = "https://ui.awin.com/merchant-profile/6561";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final String publisherId;
	private final String advertiserId;
	private final String locale;
	private final boolean configured;

	private volatile List<AwinProduct> products = List.of();
	private volatile Instant refreshedAt = Instant.EPOCH;

	public AwinImageService(
			@Qualifier("awinRestClient") RestClient awinRestClient,
			ObjectMapper objectMapper,
			@Value("${fragrance.awin.publisher-id:}") String publisherId,
			@Value("${fragrance.awin.api-token:}") String apiToken,
			@Value("${fragrance.awin.advertiser-id:6561}") String advertiserId,
			@Value("${fragrance.awin.locale:en_GB}") String locale
	) {
		this.restClient = awinRestClient;
		this.objectMapper = objectMapper;
		this.publisherId = publisherId;
		this.advertiserId = advertiserId;
		this.locale = locale;
		this.configured = !publisherId.isBlank() && !apiToken.isBlank();
	}

	public boolean isConfigured() {
		return configured;
	}

	public Optional<MediaAsset> findImage(String name, String manufacturer) {
		if (!configured) {
			return Optional.empty();
		}

		refreshIfNeeded();
		String normalizedName = normalize(name);
		String normalizedManufacturer = normalize(manufacturer);

		return products.stream()
				.filter(product -> isHttps(product.imageLink()))
				.filter(product -> normalize(product.title()).contains(normalizedName))
				.filter(product -> normalize(product.brand()).contains(normalizedManufacturer)
						|| normalize(product.title()).contains(normalizedManufacturer))
				.min(Comparator.comparingInt(product -> product.title().length()))
				.map(product -> new MediaAsset(
						product.imageLink(),
						product.link(),
						"Perfume Click via Awin",
						"Affiliate product image",
						LICENSE_URL,
						manufacturer + " " + name + " perfume bottle"
				));
	}

	private synchronized void refreshIfNeeded() {
		if (refreshedAt.plus(REFRESH_INTERVAL).isAfter(Instant.now())) {
			return;
		}

		try {
			String body = restClient.get()
					.uri(
							"/publishers/{publisherId}/awinfeeds/download/"
									+ "{advertiserId}-retail-{locale}.jsonl",
							publisherId,
							advertiserId,
							locale
					)
					.retrieve()
					.body(String.class);
			if (body == null || body.isBlank()) {
				throw new MediaProviderException("Awin returned an empty product feed", null);
			}

			products = body.lines()
					.filter(line -> !line.isBlank())
					.map(this::parseProduct)
					.flatMap(Optional::stream)
					.toList();
			refreshedAt = Instant.now();
		} catch (RestClientException exception) {
			throw new MediaProviderException("Awin product images are unavailable", exception);
		}
	}

	private Optional<AwinProduct> parseProduct(String line) {
		try {
			AwinProduct product = objectMapper.readValue(line, AwinProduct.class);
			if (product.title() == null
					|| product.brand() == null
					|| product.imageLink() == null
					|| product.link() == null) {
				return Optional.empty();
			}
			return Optional.of(product);
		} catch (JacksonException ignored) {
			return Optional.empty();
		}
	}

	private static boolean isHttps(String value) {
		return value != null && value.startsWith("https://");
	}

	private static String normalize(String value) {
		return value == null
				? ""
				: value.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{L}\\p{N}]+", " ")
				.trim();
	}

	record AwinProduct(
			String id,
			String title,
			String link,
			String brand,
			@JsonProperty("image_link") String imageLink
	) {
	}
}
