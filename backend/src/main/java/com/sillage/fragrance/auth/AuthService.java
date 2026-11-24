package com.sillage.fragrance.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {

	private final UserAccountRepository repository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserAccount register(String email, String password) {
		String normalizedEmail = normalizeEmail(email);
		if (repository.existsByEmail(normalizedEmail)) {
			throw new EmailAlreadyRegisteredException();
		}
		try {
			return repository.saveAndFlush(new UserAccount(
					normalizedEmail,
					passwordEncoder.encode(password)
			));
		} catch (DataIntegrityViolationException exception) {
			throw new EmailAlreadyRegisteredException();
		}
	}

	@Transactional(readOnly = true)
	public UserAccount authenticate(String email, String password) {
		UserAccount account = repository.findByEmail(normalizeEmail(email))
				.orElseThrow(InvalidCredentialsException::new);
		if (!passwordEncoder.matches(password, account.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}
		return account;
	}

	@Transactional(readOnly = true)
	public Optional<UserAccount> findById(Long id) {
		return repository.findById(id);
	}

	@Transactional
	public UserAccount updateProfile(long userId, UpdateProfileRequest request) {
		UserAccount account = requireAccount(userId);
		String username = optionalValue(request.username());
		if (username != null && username.length() < 2) {
			throw new IllegalArgumentException("Username must be at least 2 characters");
		}
		String usernameKey = username == null ? null : username.toLowerCase(Locale.ROOT);
		if (usernameKey != null) {
			repository.findByUsernameKey(usernameKey)
					.filter(existing -> !existing.getId().equals(userId))
					.ifPresent(existing -> {
						throw new UsernameAlreadyTakenException();
					});
		}
		account.updateProfile(
				username,
				usernameKey,
				optionalValue(request.country()),
				optionalValue(request.city())
		);
		return repository.save(account);
	}

	@Transactional
	public void updatePassword(long userId, UpdatePasswordRequest request) {
		UserAccount account = requireAccount(userId);
		if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
			throw new CurrentPasswordIncorrectException();
		}
		account.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
	}

	private UserAccount requireAccount(long userId) {
		return repository.findById(userId)
				.orElseThrow(AuthenticationRequiredException::new);
	}

	private static String optionalValue(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
