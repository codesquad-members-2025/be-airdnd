package com.team3.airdnd.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.team3.airdnd.chat.dto.ChatMessageDto;
import com.team3.airdnd.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

	private final ChatService chatService;

	@MessageMapping("/chat/message") // 클라이언트가 pub/chat/message로 전송
	@SendTo("/sub/chat/room/{reservationId}") // 예약 ID 기준으로 구독
	public ChatMessageDto sendMessage(ChatMessageDto message) {
		chatService.saveMessage(message);
		return message;
	}
}
