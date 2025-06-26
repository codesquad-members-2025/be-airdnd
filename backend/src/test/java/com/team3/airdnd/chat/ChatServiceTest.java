package com.team3.airdnd.chat;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.team3.airdnd.chat.domain.ChatRoom;
import com.team3.airdnd.chat.domain.Message;
import com.team3.airdnd.chat.dto.ChatMessageDto;
import com.team3.airdnd.chat.repository.ChatMessageRepository;
import com.team3.airdnd.chat.repository.ChatRoomRepository;
import com.team3.airdnd.chat.service.ChatService;
import com.team3.airdnd.config.MockAwsConfig;
import com.team3.airdnd.config.MockRedisConfig;
import com.team3.airdnd.reservation.domain.Reservation;
import com.team3.airdnd.reservation.repository.ReservationRepository;
import com.team3.airdnd.user.domain.User;
import com.team3.airdnd.user.repository.UserRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import({MockAwsConfig.class, MockRedisConfig.class})
class ChatServiceTest {

	@Autowired
	private ChatService chatService;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private ChatMessageRepository chatMessageRepository;

	@Test
	@DisplayName("채팅방 생성이후 채팅방이 생성되고, 서로의 메시지가 DB에 저장되어야 한다.")
	void testMessageSave() {
		// Given
		Reservation reservation = reservationRepository.findAll().get(0);
		chatService.createRoomIfNotExists(reservation);

		ChatRoom chatRoom = chatRoomRepository.findByReservationId(reservation.getId())
			.orElseThrow();

		User sender = userRepository.findById(reservation.getGuest().getId())
			.orElseThrow();

		chatService.saveMessage(ChatMessageDto.builder()
			.reservationId(reservation.getId())
			.senderId(reservation.getGuest().getId())
			.content("게스트: 안녕하세요!")
			.build());

		chatService.saveMessage(ChatMessageDto.builder()
			.reservationId(reservation.getId())
			.senderId(reservation.getAccommodation().getHost().getId())
			.content("호스트: 반갑습니다.")
			.build());

		// When
		List<Message> messages = chatMessageRepository.findByChatRoomOrderBySentAtAsc(chatRoom);

		assertThat(messages).hasSize(2);
		assertThat(messages.get(0).getContent()).contains("안녕하세요");
		assertThat(messages.get(1).getContent()).contains("반갑습니다");
	}
}
