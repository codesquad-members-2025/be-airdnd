package com.team3.airdnd.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team3.airdnd.chat.domain.ChatRoom;
import com.team3.airdnd.chat.domain.Message;

public interface ChatMessageRepository extends JpaRepository<Message, Long> {
	
	List<Message> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);
}
