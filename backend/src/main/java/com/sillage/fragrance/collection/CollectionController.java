package com.sillage.fragrance.collection;

import com.sillage.fragrance.auth.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/collection")
class CollectionController {

	private final CollectionService collectionService;
	private final AuthSession authSession;

	CollectionController(CollectionService collectionService, AuthSession authSession) {
		this.collectionService = collectionService;
		this.authSession = authSession;
	}

	@GetMapping
	CollectionResponse getCollection(HttpServletRequest request) {
		return new CollectionResponse(collectionService.findAll(authSession.requireUserId(request)));
	}

	@PostMapping("/{fragranceId}")
	ResponseEntity<Void> add(
			@PathVariable @Size(min = 1, max = 160) String fragranceId,
			HttpServletRequest request
	) {
		collectionService.add(authSession.requireUserId(request), fragranceId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{fragranceId}")
	ResponseEntity<Void> remove(
			@PathVariable @Size(min = 1, max = 160) String fragranceId,
			HttpServletRequest request
	) {
		collectionService.remove(authSession.requireUserId(request), fragranceId);
		return ResponseEntity.noContent().build();
	}
}
