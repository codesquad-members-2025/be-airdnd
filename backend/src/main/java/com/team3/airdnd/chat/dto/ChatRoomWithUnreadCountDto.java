package com.team3.airdnd.chat.dto;

import com.team3.airdnd.chat.domain.ChatRoom;
import com.team3.airdnd.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomWithUnreadCountDto {
	private Long roomId;
	private String otherUserName;
	private long unreadCount;

	public static ChatRoomWithUnreadCountDto of(ChatRoom room, Long currentUserId, long unreadCount) {
		User otherUser = getOtherUser(room, currentUserId);
		return new ChatRoomWithUnreadCountDto(
			room.getId(),
			otherUser.getUsername(),
			unreadCount
		);
	}

	private static User getOtherUser(ChatRoom room, Long currentUserId) {
		if (room.getGuest().getId().equals(currentUserId)) {
			return room.getHost();
		} else {
			return room.getGuest();
		}
	}
}
