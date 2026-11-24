package com.sillage.fragrance.collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CollectionControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void requiresAuthenticationToAddAFragrance() throws Exception {
		mockMvc.perform(post("/api/collection/creed-aventus-edp"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value(
						"Please log in to manage your collection"
				));
	}

	@Test
	void storesAndRemovesFragrancesForTheLoggedInAccount() throws Exception {
		MockHttpSession session = register("collection@example.com");

		mockMvc.perform(post("/api/collection/creed-aventus-edp").session(session))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/collection/dior-sauvage-edp").session(session))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/collection").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fragranceIds[0]").value("creed-aventus-edp"))
				.andExpect(jsonPath("$.fragranceIds[1]").value("dior-sauvage-edp"));

		mockMvc.perform(delete("/api/collection/creed-aventus-edp").session(session))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/collection").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fragranceIds.length()").value(1))
				.andExpect(jsonPath("$.fragranceIds[0]").value("dior-sauvage-edp"));
	}

	private MockHttpSession register(String email) throws Exception {
		return (MockHttpSession) mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"Scent1"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn()
				.getRequest()
				.getSession(false);
	}
}
