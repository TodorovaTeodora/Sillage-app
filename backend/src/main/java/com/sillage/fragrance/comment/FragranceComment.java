package com.sillage.fragrance.comment;

import com.sillage.fragrance.auth.UserAccount;
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
@Table(name = "fragrance_comments")
class FragranceComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 160)
	private String fragranceId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected FragranceComment() {
	}

	FragranceComment(String fragranceId, UserAccount user, String content) {
		Instant now = Instant.now();
		this.fragranceId = fragranceId;
		this.user = user;
		this.content = content;
		this.createdAt = now;
		this.updatedAt = now;
	}

	Long getId() {
		return id;
	}

	String getFragranceId() {
		return fragranceId;
	}

	UserAccount getUser() {
		return user;
	}

	String getContent() {
		return content;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}

	void updateContent(String content) {
		this.content = content;
		this.updatedAt = Instant.now();
	}
}
