package com.sillage.fragrance.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

	Optional<PasswordResetCode> findFirstByUser_IdOrderByCreatedAtDesc(Long userId);

	void deleteByUser_Id(Long userId);
}
