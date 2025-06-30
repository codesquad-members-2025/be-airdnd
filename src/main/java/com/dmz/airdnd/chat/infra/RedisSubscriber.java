package com.dmz.airdnd.chat.infra;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.dmz.airdnd.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber implements MessageListener {

	private final SimpMessagingTemplate messagingTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String payload = new String(message.getBody(), StandardCharsets.UTF_8);
			log.debug("payload: {}", payload);
			ChatMessageResponse chatMessageResponse = objectMapper.readValue(payload, ChatMessageResponse.class);

			messagingTemplate.convertAndSend("/topic/chatroom/" + chatMessageResponse.getChatRoomID(),
				chatMessageResponse);
		} catch (Exception e) {
			log.error("RedisSubscriber 메시지 처리 중 오류", e);
		}
	}
}
