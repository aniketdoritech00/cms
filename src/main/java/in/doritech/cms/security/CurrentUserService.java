
package in.doritech.cms.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class CurrentUserService {

	public static Long getCurrentUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof Long)) {

			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
		}

		return (Long) authentication.getPrincipal();
	}
}
