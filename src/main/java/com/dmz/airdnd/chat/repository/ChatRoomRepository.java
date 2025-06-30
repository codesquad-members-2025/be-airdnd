package com.dmz.airdnd.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dmz.airdnd.chat.domain.ChatRoom;
import com.dmz.airdnd.chat.dto.ChatMessageResponse;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	@Query("""
		    SELECT new com.dmz.airdnd.chat.dto.ChatMessageResponse(
				cm.chatRoom.id,
				cm.sender.id,
		        cm.sender.loginId,
		        cm.content,
				cm.sentAt
		    )
		    FROM ChatMessage cm
		    WHERE cm.chatRoom.id = :roomId
		    ORDER BY cm.sentAt ASC
		""")
	List<ChatMessageResponse> findMessagesByChatRoomId(
		@Param("roomId") Long roomId
	);
}
