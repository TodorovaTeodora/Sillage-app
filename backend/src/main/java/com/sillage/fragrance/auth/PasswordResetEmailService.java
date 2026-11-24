package com.sillage.fragrance.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
class PasswordResetEmailService {

	private final JavaMailSender mailSender;
	private final String mailHost;
	private final String senderAddress;

	PasswordResetEmailService(
			ObjectProvider<JavaMailSender> mailSender,
			@Value("${spring.mail.host:}") String mailHost,
			@Value("${sillage.mail.from:no-reply@sillage.app}") String senderAddress
	) {
		this.mailSender = mailSender.getIfAvailable();
		this.mailHost = mailHost;
		this.senderAddress = senderAddress;
	}

	void sendCode(String recipient, String code) {
		if (mailSender == null || mailHost.isBlank()) {
			throw new IllegalStateException("SMTP is not configured");
		}
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(senderAddress);
		message.setTo(recipient);
		message.setSubject("Your Sillage password reset code");
		message.setText("""
				Your Sillage password reset code is:

				%s

				This code expires in 15 minutes. If you did not request a password reset, you can ignore this email.
				""".formatted(code));
		mailSender.send(message);
	}
}
