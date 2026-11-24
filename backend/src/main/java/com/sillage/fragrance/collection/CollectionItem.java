package com.sillage.fragrance.collection;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "collection_items",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_collection_user_fragrance",
				columnNames = {"user_id", "fragrance_id"}
		)
)
class CollectionItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(name = "fragrance_id", nullable = false, length = 160)
	private String fragranceId;

	protected CollectionItem() {
	}

	CollectionItem(UserAccount user, String fragranceId) {
		this.user = user;
		this.fragranceId = fragranceId;
	}

	String getFragranceId() {
		return fragranceId;
	}
}
