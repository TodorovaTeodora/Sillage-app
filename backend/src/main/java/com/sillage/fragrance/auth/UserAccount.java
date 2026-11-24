package com.sillage.fragrance.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(nullable = false, length = 60)
	private String passwordHash;

	@Column(length = 40)
	private String username;

	@Column(unique = true, length = 40)
	private String usernameKey;

	@Column(length = 100)
	private String country;

	@Column(length = 100)
	private String city;

	protected UserAccount() {
	}

	UserAccount(String email, String passwordHash) {
		this.email = email;
		this.passwordHash = passwordHash;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	public String getUsername() {
		return username;
	}

	public String getCountry() {
		return country;
	}

	public String getCity() {
		return city;
	}

	void updateProfile(
			String username,
			String usernameKey,
			String country,
			String city
	) {
		this.username = username;
		this.usernameKey = usernameKey;
		this.country = country;
		this.city = city;
	}

	void updatePasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
