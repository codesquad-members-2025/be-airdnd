package com.dmz.airdnd.chat.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomDetailResponse {
	private Long id;
	private String name;
	private List<ChatMessageResponse> messages;
}
