package com.sillage.fragrance.auth;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.mail.host=smtp.example.com",
		"sillage.mail.from=no-reply@sillage.app"
})
@AutoConfigureMockMvc
class PasswordResetControllerTests {

	private static final Pattern CODE_PATTERN = Pattern.compile("\\b\\d{6}\\b");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JavaMailSender mailSender;

	@Test
	void sendsAResetCodeAndChangesThePassword() throws Exception {
		register("reset@example.com");

		mockMvc.perform(post("/api/auth/forgot-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"reset@example.com"}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.message").value(
						"If an account exists for this email, a reset code has been sent."
				));

		ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(message.capture());
		Matcher matcher = CODE_PATTERN.matcher(message.getValue().getText());
		assertThat(matcher.find()).isTrue();
		String code = matcher.group();

		mockMvc.perform(post("/api/auth/reset-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"reset@example.com","code":"%s","newPassword":"Changed2"}
								""".formatted(code)))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"reset@example.com","password":"Changed2"}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void rejectsAnInvalidResetCode() throws Exception {
		register("invalid-reset@example.com");
		mockMvc.perform(post("/api/auth/forgot-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid-reset@example.com"}
								"""))
				.andExpect(status().isAccepted());

		mockMvc.perform(post("/api/auth/reset-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid-reset@example.com","code":"000000","newPassword":"Changed2"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(
						"The reset code is invalid or has expired"
				));
	}

	@Test
	void doesNotRevealWhetherAnEmailExists() throws Exception {
		mockMvc.perform(post("/api/auth/forgot-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"unknown@example.com"}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.message").value(
						"If an account exists for this email, a reset code has been sent."
				));
	}

	private void register(String email) throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"Scent1"}
								""".formatted(email)))
				.andExpect(status().isCreated());
	}
}
