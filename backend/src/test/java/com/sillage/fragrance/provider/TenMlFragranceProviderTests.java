package com.sillage.fragrance.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TenMlFragranceProviderTests {

	private MockRestServiceServer server;
	private TenMlFragranceProvider provider;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		provider = new TenMlFragranceProvider(
				builder.baseUrl("https://mcp.10ml.com").build()
		);
	}

	@Test
	void mapsDetailedSearchResults() {
		server.expect(requestTo(
						"https://mcp.10ml.com/api/v1/perfumes?limit=20&format=detailed&q=Aventus"
				))
				.andExpect(method(org.springframework.http.HttpMethod.GET))
				.andRespond(withSuccess("""
						{
						  "total": 1,
						  "results": [{
						    "id": "creed-aventus-edp",
						    "name": "Aventus",
						    "house": "Creed",
						    "year": 2010,
						    "family": "Chypre fruity",
						    "character": "A confident fruity and smoky composition.",
						    "concentration": "EDP",
						    "key_notes": ["pineapple", "birch"],
						    "pyramid": {
						      "top": ["pineapple", "bergamot"],
						      "heart": ["birch"],
						      "base": ["musk", "oakmoss"]
						    }
						  }]
						}
						""", MediaType.APPLICATION_JSON));

		var result = provider.search("Aventus");

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().manufacturer()).isEqualTo("Creed");
		assertThat(result.getFirst().concentration()).isEqualTo("Eau de Parfum");
		assertThat(result.getFirst().pyramid().top())
				.containsExactly("Pineapple", "Bergamot");
		server.verify();
	}

	@Test
	void mapsDetailedLookupResultsWithMissingYearAndTiers() {
		server.expect(requestTo(
						"https://mcp.10ml.com/api/v1/perfumes/example-id?format=detailed"
				))
				.andRespond(withSuccess("""
						{
						  "found": true,
						  "exact": false,
						  "perfume": {
						    "id": "example-id",
						    "name": "Example",
						    "house": "Example House",
						    "family": "Woody",
						    "character": "An example fragrance.",
						    "concentration": "EDT",
						    "key_notes": [],
						    "pyramid": {
						      "top": [],
						      "heart": ["cedar"],
						      "base": []
						    }
						  }
						}
						""", MediaType.APPLICATION_JSON));

		var result = provider.findById("example-id");

		assertThat(result).isPresent();
		assertThat(result.orElseThrow().year()).isNull();
		assertThat(result.orElseThrow().concentration()).isEqualTo("Eau de Toilette");
		assertThat(result.orElseThrow().pyramid().heart()).containsExactly("Cedar");
		server.verify();
	}

	@Test
	void rejectsAClosestMatchWithADifferentId() {
		server.expect(requestTo(
						"https://mcp.10ml.com/api/v1/perfumes/missing-id?format=detailed"
				))
				.andRespond(withSuccess("""
						{
						  "found": true,
						  "exact": false,
						  "perfume": {
						    "id": "different-id",
						    "name": "Different",
						    "house": "Example House",
						    "family": "Woody",
						    "character": "A different fragrance.",
						    "concentration": "EDP",
						    "key_notes": [],
						    "pyramid": {"top": [], "heart": [], "base": []}
						  }
						}
						""", MediaType.APPLICATION_JSON));

		assertThat(provider.findById("missing-id")).isEmpty();
		server.verify();
	}

	@Test
	void correctsGuidance46FrankincenseNote() {
		server.expect(requestTo(
						"https://mcp.10ml.com/api/v1/perfumes/amouage-guidance-46-extrait?format=detailed"
				))
				.andRespond(withSuccess("""
						{
						  "found": true,
						  "exact": true,
						  "perfume": {
						    "id": "amouage-guidance-46-extrait",
						    "name": "Guidance 46",
						    "house": "Amouage",
						    "family": "Oriental floral",
						    "character": "A rich floral extrait.",
						    "concentration": "Extrait",
						    "key_notes": ["frankincense"],
						    "pyramid": {
						      "top": ["pear", "incense", "hazelnut"],
						      "heart": ["saffron"],
						      "base": ["vanilla"]
						    }
						  }
						}
						""", MediaType.APPLICATION_JSON));

		var result = provider.findById("amouage-guidance-46-extrait").orElseThrow();

		assertThat(result.pyramid().top())
				.containsExactly("Pear", "Frankincense", "Hazelnut");
		server.verify();
	}

	@Test
	void readsLiveCatalogCountsOnce() {
		server.expect(requestTo("https://mcp.10ml.com/api/v1/perfumes?limit=1"))
				.andRespond(withSuccess(
						"{\"total\":6018,\"results\":[]}",
						MediaType.APPLICATION_JSON
				));
		server.expect(requestTo("https://mcp.10ml.com/api/v1/houses?limit=50"))
				.andRespond(withSuccess(
						"""
								{
								  "total": 249,
								  "houses": [{"name": "Lattafa"}],
								  "next_cursor": "next-page"
								}
								""",
						MediaType.APPLICATION_JSON
				));
		server.expect(requestTo("https://mcp.10ml.com/api/v1/houses?limit=50&cursor=next-page"))
				.andRespond(withSuccess(
						"""
								{
								  "total": 249,
								  "houses": [{"name": "Chanel"}]
								}
								""",
						MediaType.APPLICATION_JSON
				));

		assertThat(provider.facets().totalCount()).isEqualTo(6018);
		assertThat(provider.facets().manufacturerCount()).isEqualTo(249);
		assertThat(provider.facets().noteCount()).isEqualTo(285);
		assertThat(provider.facets().manufacturers()).containsExactly("Chanel", "Lattafa");
		server.verify();
	}
}
