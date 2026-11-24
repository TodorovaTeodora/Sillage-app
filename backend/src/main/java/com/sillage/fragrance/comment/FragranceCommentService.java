package com.sillage.fragrance.comment;

import com.sillage.fragrance.auth.AuthService;
import com.sillage.fragrance.auth.AuthenticationRequiredException;
import com.sillage.fragrance.auth.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
class FragranceCommentService {

	private final FragranceCommentRepository repository;
	private final AuthService authService;

	FragranceCommentService(
			FragranceCommentRepository repository,
			AuthService authService
	) {
		this.repository = repository;
		this.authService = authService;
	}

	@Transactional(readOnly = true)
	List<CommentResponse> findAll(String fragranceId, Optional<Long> currentUserId) {
		return repository.findAllByFragranceIdOrderByCreatedAtAsc(fragranceId).stream()
				.map(comment -> response(comment, currentUserId))
				.toList();
	}

	@Transactional
	CommentResponse create(String fragranceId, long userId, String content) {
		UserAccount account = authService.findById(userId)
				.orElseThrow(AuthenticationRequiredException::new);
		FragranceComment comment = repository.save(
				new FragranceComment(fragranceId, account, content)
		);
		return response(comment, Optional.of(userId));
	}

	@Transactional
	CommentResponse update(
			String fragranceId,
			long commentId,
			long userId,
			String content
	) {
		FragranceComment comment = requireComment(fragranceId, commentId);
		requireOwner(comment, userId);
		comment.updateContent(content);
		return response(comment, Optional.of(userId));
	}

	@Transactional
	void delete(String fragranceId, long commentId, long userId) {
		FragranceComment comment = requireComment(fragranceId, commentId);
		requireOwner(comment, userId);
		repository.delete(comment);
	}

	private FragranceComment requireComment(String fragranceId, long commentId) {
		return repository.findById(commentId)
				.filter(comment -> comment.getFragranceId().equals(fragranceId))
				.orElseThrow(() -> new NoSuchElementException("Comment not found"));
	}

	private static void requireOwner(FragranceComment comment, long userId) {
		if (!comment.getUser().getId().equals(userId)) {
			throw new CommentAccessDeniedException();
		}
	}

	private static CommentResponse response(
			FragranceComment comment,
			Optional<Long> currentUserId
	) {
		String username = comment.getUser().getUsername();
		return new CommentResponse(
				comment.getId(),
				comment.getFragranceId(),
				username == null ? "Sillage member" : username,
				comment.getContent(),
				comment.getCreatedAt(),
				comment.getUpdatedAt(),
				currentUserId.filter(comment.getUser().getId()::equals).isPresent()
		);
	}
}
