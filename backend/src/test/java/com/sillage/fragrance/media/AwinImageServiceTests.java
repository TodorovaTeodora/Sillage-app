package com.sillage.fragrance.media;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AwinImageServiceTests {

	private MockRestServiceServer server;
	private AwinImageService service;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		service = new AwinImageService(
				builder.baseUrl("https://api.awin.com").build(),
				new ObjectMapper(),
				"12345",
				"secret-token",
				"6561",
				"en_GB"
		);
	}

	@Test
	void mapsAnExactBrandAndNameFromTheJsonlFeed() {
		server.expect(requestTo(
						"https://api.awin.com/publishers/12345/awinfeeds/download/"
								+ "6561-retail-en_GB.jsonl"
				))
				.andRespond(withSuccess("""
						{"id":"1","title":"Other Fragrance 100ml","brand":"Other","link":"https://shop.example/other","image_link":"https://images.example/other.jpg"}
						{"id":"2","title":"Amouage Epic Man Eau de Parfum 100ml","brand":"Amouage","link":"https://shop.example/epic","image_link":"https://images.example/epic.jpg"}
						""", MediaType.TEXT_PLAIN));

		var image = service.findImage("Epic Man", "Amouage");

		assertThat(image).isPresent();
		assertThat(image.orElseThrow().imageUrl())
				.isEqualTo("https://images.example/epic.jpg");
		assertThat(image.orElseThrow().sourcePageUrl())
				.isEqualTo("https://shop.example/epic");
		server.verify();
	}

	@Test
	void skipsTheFeedWhenCredentialsAreMissing() {
		AwinImageService unconfigured = new AwinImageService(
				RestClient.builder().baseUrl("https://api.awin.com").build(),
				new ObjectMapper(),
				"",
				"",
				"6561",
				"en_GB"
		);

		assertThat(unconfigured.findImage("Epic Man", "Amouage")).isEmpty();
	}
}
