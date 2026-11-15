
package com.demo.department.security;

import com.demo.department.config.Constants;
import com.demo.department.config.HttpConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return HttpConstants.OPTIONS.equalsIgnoreCase(request.getMethod())
                || path.startsWith(Constants.AUTH_ENDPOINT + "/")
                || path.equals(Constants.AUTH_ENDPOINT)
                || path.startsWith(Constants.SWAGGER_UI_PATH)
                || path.startsWith(Constants.API_DOCS_PATH)
                || path.equals(Constants.ACTUATOR_HEALTH_PATH);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String role;

        try {
            String authorization =
                    request.getHeader(Constants.AUTHORIZATION);

            if (authorization == null
                    || !authorization.startsWith(Constants.BEARER_PREFIX)) {

                response.sendError(
                        Constants.HTTP_UNAUTHORIZED,
                        Constants.BEARER_TOKEN_REQUIRED
                );
                return;
            }

            role = jwt.role(
                    authorization.substring(Constants.BEARER_PREFIX_LENGTH)
            );

        } catch (Exception exception) {

            response.sendError(
                    Constants.HTTP_UNAUTHORIZED,
                    Constants.INVALID_EXPIRED_JWT
            );
            return;
        }

        if (!Constants.ADMIN.equals(role)
                && !Constants.USER.equals(role)) {

            response.sendError(
                    Constants.HTTP_FORBIDDEN,
                    Constants.UNSUPPORTED_ROLE
            );
            return;
        }

        if (!HttpConstants.GET.equalsIgnoreCase(request.getMethod())
                && !Constants.ADMIN.equals(role)) {

            response.sendError(
                    Constants.HTTP_FORBIDDEN,
                    Constants.ADMIN_ROLE_REQUIRED
            );
            return;
        }

        request.setAttribute(Constants.AUTHENTICATED_USER, true);

        chain.doFilter(request, response);
    }
}
