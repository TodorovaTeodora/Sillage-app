package com.sillage.fragrance.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
class PasswordResetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);
	private static final Duration CODE_LIFETIME = Duration.ofMinutes(15);
	private static final int MAX_FAILED_ATTEMPTS = 5;

	private final UserAccountRepository userRepository;
	private final PasswordResetCodeRepository codeRepository;
	private final PasswordResetEmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	PasswordResetService(
			UserAccountRepository userRepository,
			PasswordResetCodeRepository codeRepository,
			PasswordResetEmailService emailService,
			PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.codeRepository = codeRepository;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void requestCode(String email) {
		userRepository.findByEmail(normalizeEmail(email)).ifPresent(account -> {
			codeRepository.deleteByUser_Id(account.getId());
			String code = "%06d".formatted(secureRandom.nextInt(1_000_000));
			PasswordResetCode resetCode = codeRepository.saveAndFlush(
					new PasswordResetCode(
							account,
							passwordEncoder.encode(code),
							Instant.now().plus(CODE_LIFETIME)
					)
			);
			try {
				emailService.sendCode(account.getEmail(), code);
			} catch (RuntimeException exception) {
				codeRepository.delete(resetCode);
				LOGGER.error("Unable to send password reset email for account {}", account.getId(), exception);
			}
		});
	}

	@Transactional(noRollbackFor = InvalidResetCodeException.class)
	public void resetPassword(String email, String code, String newPassword) {
		UserAccount account = userRepository.findByEmail(normalizeEmail(email))
				.orElseThrow(InvalidResetCodeException::new);
		PasswordResetCode resetCode = codeRepository
				.findFirstByUser_IdOrderByCreatedAtDesc(account.getId())
				.orElseThrow(InvalidResetCodeException::new);

		if (resetCode.getUsedAt() != null
				|| resetCode.getExpiresAt().isBefore(Instant.now())
				|| resetCode.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
			throw new InvalidResetCodeException();
		}
		if (!passwordEncoder.matches(code, resetCode.getCodeHash())) {
			resetCode.recordFailedAttempt();
			throw new InvalidResetCodeException();
		}

		account.updatePasswordHash(passwordEncoder.encode(newPassword));
		resetCode.markUsed();
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
