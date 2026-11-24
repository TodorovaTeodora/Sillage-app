package com.sillage.fragrance.comment;

import java.time.Instant;

record CommentResponse(
		long id,
		String fragranceId,
		String author,
		String content,
		Instant createdAt,
		Instant updatedAt,
		boolean ownedByCurrentUser
) {
}
