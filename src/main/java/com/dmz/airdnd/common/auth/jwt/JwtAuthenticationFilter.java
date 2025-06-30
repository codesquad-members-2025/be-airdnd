package com.dmz.airdnd.common.auth.jwt;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.dmz.airdnd.common.auth.UserContext;
import com.dmz.airdnd.common.auth.dto.UserInfo;
import com.dmz.airdnd.common.dto.ApiResponse;
import com.dmz.airdnd.common.exception.ErrorCode;
import com.dmz.airdnd.common.exception.JwtValidationException;
import com.dmz.airdnd.user.domain.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements Filter {

	private static final int BEARER_PREFIX_LENGTH = 7;

	private final JwtUtil jwtUtil;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
		HttpServletResponse httpResponse = (HttpServletResponse)servletResponse;

		String requestUri = httpRequest.getRequestURI();
		String method = httpRequest.getMethod();

		// OPTIONS 요청 우회
		if ("OPTIONS".equalsIgnoreCase(method)) {
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
			httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
			httpResponse.setHeader("Access-Control-Allow-Headers",
				httpRequest.getHeader("Access-Control-Request-Headers"));
			httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
			return;
		}

		// 인증 예외 경로 우회
		if (isPermitAllPath(requestUri)) {
			filterChain.doFilter(httpRequest, httpResponse);
			return;
		}

		// Authorization 헤더 검사
		String authHeader = httpRequest.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			writeJsonResponse(httpResponse,
				ApiResponse.failure(ErrorCode.EMPTY_JWT_TOKEN.getMessage(), ErrorCode.EMPTY_JWT_TOKEN.getCode())
			);
			return;
		}

		// 토큰 유효성 검사
		String accessToken = authHeader.substring(BEARER_PREFIX_LENGTH);
		try {
			Claims claims = jwtUtil.validateToken(accessToken);
			UserContext.set(
				new UserInfo(Long.valueOf(claims.getSubject()), Role.valueOf(claims.get("role", String.class))));

			filterChain.doFilter(httpRequest, httpResponse);
		} catch (JwtValidationException e) {
			writeJsonResponse(httpResponse,
				ApiResponse.failure(e.getMessage(), ErrorCode.INVALID_JWT_TOKEN.getCode())
			);
		} catch (Exception e) {
			writeJsonResponse(httpResponse,
				ApiResponse.failure(e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR.getCode())
			);
		} finally {
			UserContext.clear();
		}
	}

	private boolean isPermitAllPath(String uri) {
		return uri.startsWith("/api/auth") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")
			|| uri.startsWith("/ws");

	}

	private void writeJsonResponse(HttpServletResponse response, ApiResponse<Void> dto) throws
		IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json; charset=UTF-8");
		String json = new ObjectMapper().writeValueAsString(dto);
		response.getWriter().write(json);
	}
}
