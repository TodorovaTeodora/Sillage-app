package com.sillage.fragrance.comment;

public class CommentAccessDeniedException extends RuntimeException {

	public CommentAccessDeniedException() {
		super("You can only edit or delete your own comments");
	}
}
