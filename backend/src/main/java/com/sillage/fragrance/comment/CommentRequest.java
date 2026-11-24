package com.sillage.fragrance.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CommentRequest(
		@NotBlank
		@Size(max = 1000)
		String content
) {
	public CommentRequest {
		content = content == null ? null : content.trim();
	}
}
