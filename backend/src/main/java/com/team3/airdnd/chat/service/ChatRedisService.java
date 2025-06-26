package com.team3.airdnd.chat.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRedisService {

	private final RedisTemplate<String, Object> redisTemplate;

	public void incrementUnreadCount(Long roomId, Long targetUserId) {
		String key = getUnreadKey(roomId, targetUserId);
		redisTemplate.opsForValue().increment(key);
	}

	public void clearUnreadCount(Long roomId, Long userId) {
		String key = getUnreadKey(roomId, userId);
		redisTemplate.delete(key);
	}

	public long getUnreadCount(Long roomId, Long userId) {
		String key = getUnreadKey(roomId, userId);
		Object value = redisTemplate.opsForValue().get(key);
		return value != null ? Long.parseLong(value.toString()) : 0;
	}

	private String getUnreadKey(Long roomId, Long userId) {
		return "unread:room:" + roomId + ":user:" + userId;
	}
}
