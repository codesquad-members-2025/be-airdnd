package com.team3.airdnd.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team3.airdnd.chat.dto.ChatMessageResponseDto;
import com.team3.airdnd.chat.dto.ChatRoomWithUnreadCountDto;
import com.team3.airdnd.chat.service.ChatService;
import com.team3.airdnd.global.dto.ResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

	private final ChatService chatService;

	@GetMapping("/rooms/{reservationId}/messages")
	public ResponseEntity<ResponseDto<List<ChatMessageResponseDto>>> getMessages(@PathVariable Long reservationId) {
		List<ChatMessageResponseDto> messages = chatService.getMessageList(reservationId);
		return ResponseDto.ok(messages);
	}

	// 안읽은 메시지 읽음 처리
	@PostMapping("/rooms/{roomId}/read")
	public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long roomId) {
		Long userId = 1L;
		chatService.markMessagesAsRead(roomId, userId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/rooms")
	public ResponseEntity<ResponseDto<List<ChatRoomWithUnreadCountDto>>> getMyChatRooms(
		@RequestParam(value = "unreadOnly", required = false, defaultValue = "false") boolean unreadOnly
	) {
		//unreadOnly=false: 전체 채팅방 목록 + 각 채팅방의 읽지 않은 메시지 수
		//unreadOnly=true: 읽지 않은 메시지가 있는 채팅방만
		Long userId = 1L;
		List<ChatRoomWithUnreadCountDto> chatRooms = chatService.getMyChatRooms(userId, unreadOnly);
		return ResponseDto.ok(chatRooms);
	}
}
