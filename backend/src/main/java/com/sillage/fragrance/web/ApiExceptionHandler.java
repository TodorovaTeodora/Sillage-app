package com.sillage.fragrance.web;

import com.sillage.fragrance.auth.EmailAlreadyRegisteredException;
import com.sillage.fragrance.auth.AuthenticationRequiredException;
import com.sillage.fragrance.auth.CurrentPasswordIncorrectException;
import com.sillage.fragrance.auth.InvalidCredentialsException;
import com.sillage.fragrance.auth.UsernameAlreadyTakenException;
import com.sillage.fragrance.comment.CommentAccessDeniedException;
import jakarta.validation.ConstraintViolationException;
import com.sillage.fragrance.provider.FragranceProviderException;
import com.sillage.fragrance.media.MediaProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NoSuchElementException.class)
	ProblemDetail handleNotFound(NoSuchElementException exception) {
		return problem(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
	ProblemDetail handleBadRequest(Exception exception) {
		return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleInvalidRequest(MethodArgumentNotValidException exception) {
		String detail = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("The request is invalid");
		return problem(HttpStatus.BAD_REQUEST, detail);
	}

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	ProblemDetail handleEmailConflict(EmailAlreadyRegisteredException exception) {
		return problem(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
		return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(AuthenticationRequiredException.class)
	ProblemDetail handleAuthenticationRequired(AuthenticationRequiredException exception) {
		return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(CurrentPasswordIncorrectException.class)
	ProblemDetail handleCurrentPasswordIncorrect(CurrentPasswordIncorrectException exception) {
		return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(UsernameAlreadyTakenException.class)
	ProblemDetail handleUsernameConflict(UsernameAlreadyTakenException exception) {
		return problem(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(CommentAccessDeniedException.class)
	ProblemDetail handleCommentAccessDenied(CommentAccessDeniedException exception) {
		return problem(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(FragranceProviderException.class)
	ProblemDetail handleProviderFailure(FragranceProviderException exception) {
		return problem(HttpStatus.BAD_GATEWAY, exception.getMessage());
	}

	@ExceptionHandler(MediaProviderException.class)
	ProblemDetail handleMediaProviderFailure(MediaProviderException exception) {
		return problem(HttpStatus.BAD_GATEWAY, exception.getMessage());
	}

	private static ProblemDetail problem(HttpStatus status, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(status.getReasonPhrase());
		return problem;
	}
}
