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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsPasswordsThatDoNotMeetRequirements() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"weak@example.com","password":"weak"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(
						"Password must be at least 5 characters and include an uppercase letter and a number"
				));
	}

	@Test
	void registersLogsInAndRestoresTheSession() throws Exception {
		MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":" Person@Example.com ","password":"Scent1"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("person@example.com"))
				.andReturn()
				.getRequest()
				.getSession(false);

		mockMvc.perform(get("/api/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("person@example.com"));
	}

	@Test
	void rejectsInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"missing@example.com","password":"Wrong1"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Invalid email or password"));
	}

	@Test
	void logsOutAndLogsBackIn() throws Exception {
		String account = """
				{"email":"login@example.com","password":"Scent1"}
				""";
		MockHttpSession registeredSession = (MockHttpSession) mockMvc.perform(
						post("/api/auth/register")
								.contentType(MediaType.APPLICATION_JSON)
								.content(account))
				.andExpect(status().isCreated())
				.andReturn()
				.getRequest()
				.getSession(false);

		mockMvc.perform(post("/api/auth/logout").session(registeredSession))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());

		MockHttpSession loginSession = (MockHttpSession) mockMvc.perform(
						post("/api/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(account))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("login@example.com"))
				.andReturn()
				.getRequest()
				.getSession(false);

		mockMvc.perform(get("/api/auth/me").session(loginSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("login@example.com"));
	}

	@Test
	void rejectsAnEmailThatIsAlreadyRegistered() throws Exception {
		String account = """
				{"email":"duplicate@example.com","password":"Scent1"}
				""";
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(account))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(account))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value(
						"An account with this email already exists"
				));
	}
}
