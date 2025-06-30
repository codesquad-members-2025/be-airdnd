package com.dmz.airdnd.chat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageRequest {

	@NotNull
	@Min(0)
	private Long roomId;

	@NotNull
	@NotBlank
	@Size(max = 500, message = "메시지는 최대 500자까지 입력 가능합니다.")
	private String content;
}
