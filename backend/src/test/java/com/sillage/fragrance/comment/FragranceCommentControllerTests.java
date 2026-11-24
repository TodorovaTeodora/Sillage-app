package com.sillage.fragrance.comment;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FragranceCommentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void requiresAuthenticationToComment() throws Exception {
		mockMvc.perform(post("/api/fragrances/creed-aventus-edp/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"content":"Excellent fragrance ✨"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void createsEditsAndDeletesAnOwnedComment() throws Exception {
		MockHttpSession session = register("comment-owner@example.com");

		String location = mockMvc.perform(post("/api/fragrances/creed-aventus-edp/comments")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"content":"Excellent fragrance ✨"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.author").value("Sillage member"))
				.andExpect(jsonPath("$.ownedByCurrentUser").value(true))
				.andReturn()
				.getResponse()
				.getContentAsString();
		long commentId = tools.jackson.databind.json.JsonMapper.builder()
				.build()
				.readTree(location)
				.get("id")
				.asLong();

		mockMvc.perform(put("/api/fragrances/creed-aventus-edp/comments/{id}", commentId)
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"content":"Updated comment ❤️"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").value("Updated comment ❤️"));

		mockMvc.perform(delete("/api/fragrances/creed-aventus-edp/comments/{id}", commentId)
						.session(session))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/fragrances/creed-aventus-edp/comments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void preventsEditingAnotherUsersComment() throws Exception {
		MockHttpSession owner = register("comment-first@example.com");
		MockHttpSession other = register("comment-second@example.com");
		String response = mockMvc.perform(post("/api/fragrances/dior-sauvage-edp/comments")
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"content":"My comment"}
								"""))
				.andReturn()
				.getResponse()
				.getContentAsString();
		long commentId = tools.jackson.databind.json.JsonMapper.builder()
				.build()
				.readTree(response)
				.get("id")
				.asLong();

		mockMvc.perform(delete("/api/fragrances/dior-sauvage-edp/comments/{id}", commentId)
						.session(other))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.detail").value(
						"You can only edit or delete your own comments"
				));
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
