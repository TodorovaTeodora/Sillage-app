package com.sillage.fragrance.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "password_reset_codes")
class PasswordResetCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(nullable = false, length = 60)
	private String codeHash;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant usedAt;

	@Column(nullable = false)
	private int failedAttempts;

	protected PasswordResetCode() {
	}

	PasswordResetCode(UserAccount user, String codeHash, Instant expiresAt) {
		this.user = user;
		this.codeHash = codeHash;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	String getCodeHash() {
		return codeHash;
	}

	Instant getExpiresAt() {
		return expiresAt;
	}

	Instant getUsedAt() {
		return usedAt;
	}

	int getFailedAttempts() {
		return failedAttempts;
	}

	void recordFailedAttempt() {
		failedAttempts++;
	}

	void markUsed() {
		usedAt = Instant.now();
	}
}
