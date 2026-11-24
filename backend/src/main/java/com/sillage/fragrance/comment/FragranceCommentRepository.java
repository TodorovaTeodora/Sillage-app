package com.sillage.fragrance.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface FragranceCommentRepository extends JpaRepository<FragranceComment, Long> {

	List<FragranceComment> findAllByFragranceIdOrderByCreatedAtAsc(String fragranceId);
}
