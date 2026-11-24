package com.sillage.fragrance.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void updatesAndClearsProfileDetails() throws Exception {
		MockHttpSession session = register("profile@example.com");

		mockMvc.perform(put("/api/account")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"Scent Lover","country":"Bulgaria","city":"Sofia"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("Scent Lover"))
				.andExpect(jsonPath("$.country").value("Bulgaria"))
				.andExpect(jsonPath("$.city").value("Sofia"));

		mockMvc.perform(put("/api/account")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"","country":"","city":""}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").doesNotExist())
				.andExpect(jsonPath("$.country").doesNotExist())
				.andExpect(jsonPath("$.city").doesNotExist());
	}

	@Test
	void requiresUniqueUsernames() throws Exception {
		MockHttpSession first = register("first-profile@example.com");
		MockHttpSession second = register("second-profile@example.com");
		String profile = """
				{"username":"Collector","country":"","city":""}
				""";

		mockMvc.perform(put("/api/account")
						.session(first)
						.contentType(MediaType.APPLICATION_JSON)
						.content(profile))
				.andExpect(status().isOk());
		mockMvc.perform(put("/api/account")
						.session(second)
						.contentType(MediaType.APPLICATION_JSON)
						.content(profile))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("This username is already in use"));
	}

	@Test
	void changesPasswordOnlyWhenTheCurrentPasswordMatches() throws Exception {
		MockHttpSession session = register("password-profile@example.com");

		mockMvc.perform(put("/api/account/password")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"currentPassword":"Wrong1","newPassword":"Changed2"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Current password is incorrect"));

		mockMvc.perform(put("/api/account/password")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"currentPassword":"Scent1","newPassword":"Changed2"}
								"""))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"password-profile@example.com","password":"Changed2"}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void requiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/account"))
				.andExpect(status().isUnauthorized());
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
