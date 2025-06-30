package com.dmz.airdnd.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmz.airdnd.chat.domain.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
