package com.sillage.fragrance.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByEmail(String email);

	Optional<UserAccount> findByUsernameKey(String usernameKey);

	boolean existsByEmail(String email);
}
