package com.sillage.fragrance.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {

	List<CollectionItem> findAllByUser_IdOrderById(Long userId);

	boolean existsByUser_IdAndFragranceId(Long userId, String fragranceId);

	void deleteByUser_IdAndFragranceId(Long userId, String fragranceId);
}
