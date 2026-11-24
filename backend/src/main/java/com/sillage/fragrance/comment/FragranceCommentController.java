package com.sillage.fragrance.comment;

import com.sillage.fragrance.auth.AuthSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/fragrances/{fragranceId}/comments")
class FragranceCommentController {

	private final FragranceCommentService commentService;
	private final AuthSession authSession;

	FragranceCommentController(
			FragranceCommentService commentService,
			AuthSession authSession
	) {
		this.commentService = commentService;
		this.authSession = authSession;
	}

	@GetMapping
	List<CommentResponse> comments(
			@PathVariable @Size(min = 1, max = 160) String fragranceId,
			HttpServletRequest request
	) {
		return commentService.findAll(fragranceId, authSession.userId(request));
	}

	@PostMapping
	ResponseEntity<CommentResponse> create(
			@PathVariable @Size(min = 1, max = 160) String fragranceId,
			@Valid @RequestBody CommentRequest comment,
			HttpServletRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(
				commentService.create(
						fragranceId,
						authSession.requireUserId(request),
						comment.content()
				)
		);
	}

	@PutMapping("/{commentId}")
	CommentResponse update(
			@PathVariable @Size(min = 1, max = 160) String fragranceId,
			@PathVariable long commentId,
			@Valid @RequestBody CommentRequest comment,
			HttpServletRequest request
	) {
		return commentService.update(
				fragranceId,
				commentId,
				authSession.requireUserId(request),
				comment.content()
		);
	}

	@DeleteMapping("/{commentId}")
	ResponseEntity<Void> delete(
			@PathVariable @Size(min = 1, max = 160) String fragranceId,
			@PathVariable long commentId,
			HttpServletRequest request
	) {
		commentService.delete(
				fragranceId,
				commentId,
				authSession.requireUserId(request)
		);
		return ResponseEntity.noContent().build();
	}
}
