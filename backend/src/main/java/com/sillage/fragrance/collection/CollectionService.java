package com.sillage.fragrance.collection;

import com.sillage.fragrance.auth.AuthService;
import com.sillage.fragrance.auth.AuthenticationRequiredException;
import com.sillage.fragrance.auth.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class CollectionService {

	private final CollectionItemRepository repository;
	private final AuthService authService;

	CollectionService(CollectionItemRepository repository, AuthService authService) {
		this.repository = repository;
		this.authService = authService;
	}

	@Transactional(readOnly = true)
	List<String> findAll(long userId) {
		return repository.findAllByUser_IdOrderById(userId).stream()
				.map(CollectionItem::getFragranceId)
				.toList();
	}

	@Transactional
	void add(long userId, String fragranceId) {
		if (repository.existsByUser_IdAndFragranceId(userId, fragranceId)) {
			return;
		}
		UserAccount account = authService.findById(userId)
				.orElseThrow(AuthenticationRequiredException::new);
		repository.saveAndFlush(new CollectionItem(account, fragranceId));
	}

	@Transactional
	void remove(long userId, String fragranceId) {
		repository.deleteByUser_IdAndFragranceId(userId, fragranceId);
	}
}
