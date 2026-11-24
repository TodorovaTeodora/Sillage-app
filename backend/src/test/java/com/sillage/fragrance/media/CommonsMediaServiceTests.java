package com.sillage.fragrance.media;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CommonsMediaServiceTests {

	private MockRestServiceServer server;
	private CommonsMediaService service;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		AwinImageService awinImageService = new AwinImageService(
				RestClient.builder().baseUrl("https://api.awin.com").build(),
				new ObjectMapper(),
				"",
				"",
				"6561",
				"en_GB"
		);
		service = new CommonsMediaService(
				builder.baseUrl("https://commons.wikimedia.org").build(),
				awinImageService
		);
	}

	@Test
	void mapsLicensedFragranceAndNoteImages() {
		server.expect(request -> assertThat(request.getURI().getQuery())
						.contains("gsrsearch=Creed Aventus perfume bottle"))
				.andRespond(withSuccess("""
						{
						  "query": {
						    "pages": [{
						      "index": 1,
						      "title": "File:Creed Aventus perfume bottle.jpg",
						      "imageinfo": [{
						        "thumburl": "https://upload.wikimedia.org/aventus.jpg",
						        "descriptionurl": "https://commons.wikimedia.org/wiki/File:Aventus.jpg",
						        "extmetadata": {
						          "Artist": {"value": "<b>Example Photographer</b>"},
						          "LicenseShortName": {"value": "CC BY-SA 4.0"},
						          "LicenseUrl": {"value": "https://creativecommons.org/licenses/by-sa/4.0"}
						        }
						      }]
						    }]
						  }
						}
						""", MediaType.APPLICATION_JSON));
		server.expect(request -> assertThat(request.getURI().getQuery())
						.contains("gsrsearch=Citrus bergamia fruit"))
				.andRespond(withSuccess("""
						{
						  "query": {
						    "pages": [{
						      "index": 1,
						      "title": "File:Bergamot fruit.jpg",
						      "imageinfo": [{
						        "thumburl": "https://thumb.wikimedia.org/bergamot.jpg",
						        "descriptionurl": "https://commons.wikimedia.org/wiki/File:Bergamot.jpg",
						        "extmetadata": {
						          "Artist": {"value": "Botanical Artist"},
						          "LicenseShortName": {"value": "Public domain"}
						        }
						      }]
						    }]
						  }
						}
						""", MediaType.APPLICATION_JSON));

		FragranceMedia result = service.findMedia(
				new FragranceMediaRequest("Aventus", "Creed", List.of("Bergamot"))
		);

		assertThat(result.fragranceImage()).isNotNull();
		assertThat(result.fragranceImage().author()).isEqualTo("Example Photographer");
		assertThat(result.noteImages()).containsKey("Bergamot");
		assertThat(result.noteImages().get("Bergamot").licenseName())
				.isEqualTo("Public domain");
		server.verify();
	}

	@Test
	void rejectsAnUnrelatedPerfumeImage() {
		server.expect(request -> {
				})
				.andRespond(withSuccess("""
						{
						  "query": {
						    "pages": [{
						      "index": 1,
						      "title": "File:Unrelated perfume bottle.jpg",
						      "imageinfo": [{
						        "thumburl": "https://upload.wikimedia.org/unrelated.jpg",
						        "descriptionurl": "https://commons.wikimedia.org/wiki/File:Unrelated.jpg",
						        "extmetadata": {
						          "Artist": {"value": "Example"},
						          "LicenseShortName": {"value": "CC0"}
						        }
						      }]
						    }]
						  }
						}
						""", MediaType.APPLICATION_JSON));

		FragranceMedia result = service.findMedia(
				new FragranceMediaRequest("Decision 42", "Amouage", List.of())
		);

		assertThat(result.fragranceImage()).isNull();
		server.verify();
	}

	@Test
	void usesRelevantPhotographsForSpecificNotes() {
		server.expect(request -> assertThat(request.getURI().getQuery())
						.contains("gsrsearch=Test House Test perfume bottle"))
				.andRespond(withSuccess("""
						{"query":{"pages":[]}}
						""", MediaType.APPLICATION_JSON));
		server.expect(request -> assertThat(request.getURI().getQuery())
						.contains("gsrsearch=Juniperus communis berries"))
				.andRespond(withSuccess("""
						{
						  "query": {
						    "pages": [{
						      "index": 1,
						      "title": "File:Juniperus communis Finland 2006.jpg",
						      "imageinfo": [{
						        "thumburl": "https://thumb.wikimedia.org/juniper.jpg",
						        "descriptionurl": "https://commons.wikimedia.org/wiki/File:Juniperus_communis.jpg",
						        "extmetadata": {
						          "Artist": {"value": "Botanical Photographer"},
						          "LicenseShortName": {"value": "CC BY 2.5"}
						        }
						      }]
						    }]
						  }
						}
						""", MediaType.APPLICATION_JSON));
		server.expect(request -> assertThat(request.getURI().getQuery())
						.contains("gsrsearch=Cedrus libani wood"))
				.andRespond(withSuccess("""
						{
						  "query": {
						    "pages": [{
						      "index": 1,
						      "title": "File:Cedrus wood.jpg",
						      "imageinfo": [{
						        "thumburl": "https://thumb.wikimedia.org/cedrus-wood.jpg",
						        "descriptionurl": "https://commons.wikimedia.org/wiki/File:Cedrus_wood.jpg",
						        "extmetadata": {
						          "Artist": {"value": "Wood Photographer"},
						          "LicenseShortName": {"value": "CC BY-SA 3.0"}
						        }
						      }]
						    }]
						  }
						}
						""", MediaType.APPLICATION_JSON));
		server.expect(request -> assertThat(request.getURI().getQuery())
						.contains("gsrsearch=vanilla beans pods"))
				.andRespond(withSuccess("""
						{
						  "query": {
						    "pages": [{
						      "index": 1,
						      "title": "File:Vanilla 6beans.JPG",
						      "imageinfo": [{
						        "thumburl": "https://thumb.wikimedia.org/vanilla-beans.jpg",
						        "descriptionurl": "https://commons.wikimedia.org/wiki/File:Vanilla_6beans.JPG",
						        "extmetadata": {
						          "Artist": {"value": "Food Photographer"},
						          "LicenseShortName": {"value": "CC BY-SA 3.0"}
						        }
						      }]
						    }]
						  }
						}
						""", MediaType.APPLICATION_JSON));

		FragranceMedia result = service.findMedia(
				new FragranceMediaRequest(
						"Test",
						"Test House",
						List.of("Juniper berry", "Cedarwood", "Vanilla")
				)
		);

		assertThat(result.noteImages().get("Juniper berry").sourcePageUrl())
				.endsWith("File:Juniperus_communis.jpg");
		assertThat(result.noteImages().get("Cedarwood").sourcePageUrl())
				.endsWith("File:Cedrus_wood.jpg");
		assertThat(result.noteImages().get("Vanilla").sourcePageUrl())
				.endsWith("File:Vanilla_6beans.JPG");
		server.verify();
	}
}
