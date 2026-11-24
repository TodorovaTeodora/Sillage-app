package com.sillage.fragrance.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthSession {

	private static final String USER_ID = "userId";

	public void signIn(HttpServletRequest request, UserAccount account) {
		HttpSession session = request.getSession(true);
		request.changeSessionId();
		session.setAttribute(USER_ID, account.getId());
	}

	public Optional<Long> userId(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return Optional.empty();
		}
		Object userId = session.getAttribute(USER_ID);
		return userId instanceof Long id ? Optional.of(id) : Optional.empty();
	}

	public long requireUserId(HttpServletRequest request) {
		return userId(request).orElseThrow(AuthenticationRequiredException::new);
	}

	public void signOut(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}
}
