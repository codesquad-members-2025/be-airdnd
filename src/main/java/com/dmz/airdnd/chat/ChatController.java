package com.dmz.airdnd.chat;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.dmz.airdnd.chat.domain.ChatMessage;
import com.dmz.airdnd.chat.domain.ChatRoom;
import com.dmz.airdnd.chat.dto.ChatMessageRequest;
import com.dmz.airdnd.chat.dto.ChatMessageResponse;
import com.dmz.airdnd.chat.infra.RedisPublisher;
import com.dmz.airdnd.chat.repository.ChatMessageRepository;
import com.dmz.airdnd.chat.repository.ChatRoomRepository;
import com.dmz.airdnd.common.exception.ChatRoomNotFoundException;
import com.dmz.airdnd.common.exception.ErrorCode;
import com.dmz.airdnd.common.exception.UserNotFoundException;
import com.dmz.airdnd.user.domain.User;
import com.dmz.airdnd.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	private final RedisPublisher redisPublisher;
	private final ObjectMapper objectMapper;

	@MessageMapping("/chat.send")
	public void sendMessage(@Valid ChatMessageRequest request, MessageHeaders headers) throws JsonProcessingException {
		SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(headers,
			SimpMessageHeaderAccessor.class);
		Long userId = (Long)accessor.getSessionAttributes().get("userId");

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

		ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
			.orElseThrow(() -> new ChatRoomNotFoundException(ErrorCode.CHAT_ROOM_NOT_FOUND));

		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoom(chatRoom)
			.content(request.getContent())
			.sender(user)
			.build();

		ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

		redisPublisher.publish("chatroom." + request.getRoomId(),
			objectMapper.writeValueAsString(ChatMessageResponse.from(savedMessage)));
	}
}
