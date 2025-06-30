package com.dmz.airdnd.chat.dto;

import java.time.LocalDateTime;

import com.dmz.airdnd.chat.domain.ChatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
	private Long chatRoomID;
	private Long senderId;
	private String senderName;
	private String content;
	private LocalDateTime createdAt;

	public static ChatMessageResponse from(ChatMessage message) {
		return ChatMessageResponse.builder()
			.chatRoomID(message.getChatRoom().getId())
			.senderId(message.getSender().getId())
			.senderName(message.getSender().getLoginId())
			.content(message.getContent())
			.createdAt(message.getSentAt())
			.build();
	}
}
