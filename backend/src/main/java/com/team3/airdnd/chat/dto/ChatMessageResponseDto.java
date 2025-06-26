package com.team3.airdnd.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponseDto {
	private Long messageId;
	private Long senderId;
	private String senderName;
	private String content;
	private LocalDateTime sentAt;
}
