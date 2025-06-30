package com.dmz.airdnd.chat;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.dmz.airdnd.common.auth.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtUtil jwtUtil;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes) {

		URI uri = request.getURI();
		String query = uri.getQuery();

		if (query != null && query.contains("token=")) {
			String token = Arrays.stream(query.split("&"))
				.filter(p -> p.startsWith("token="))
				.findFirst()
				.map(p -> p.substring("token=".length()))
				.orElse(null);

			if (token != null) {
				try {
					Long userId = Long.valueOf(jwtUtil.validateToken(token).getSubject());
					attributes.put("userId", userId);
				} catch (Exception e) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Exception exception) {
		// 연결 성공/실패 후 로그 출력
		System.out.println("WebSocket handshake completed");
	}
}
