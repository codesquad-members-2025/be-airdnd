package rice_monkey.messaging.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import rice_monkey.messaging.dto.ChatMessageDto;
import rice_monkey.messaging.dto.ChatRoomListGetResponse;
import rice_monkey.messaging.dto.MessageSubDto;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Redis에서 메시지가 발행(publish)되면
     * 대기하고 있던 Redis Subscriber가 해당 메시지를 받아 처리한다.
     */
    public void sendMessage(String publishMessage) {
        try {

            ChatMessageDto chatMessage =
                    objectMapper.readValue(publishMessage, MessageSubDto.class).getChatMessageDto();

            log.info("Redis Subcriber chatMSG : {}", chatMessage.getMessage());

            // 채팅방을 구독한 클라이언트에게 메시지 발송
            messagingTemplate.convertAndSend(
                    "/sub/chat/room/" + chatMessage.getRoomId(), chatMessage
            );

        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }

    public void sendRoomList(String publishMessage) {
        try {
            MessageSubDto dto = objectMapper.readValue(publishMessage, MessageSubDto.class);

            List<ChatRoomListGetResponse> chatRoomListGetResponseList = dto.getList();
            List<ChatRoomListGetResponse> chatRoomListGetResponseListPartner = dto.getPartnerList();

            Long userId = dto.getUserId();
            Long partnerId = dto.getPartnerId();

            // 로그인 유저 채팅방 리스트 최신화 -> 내 계정에 보냄
            messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + userId, chatRoomListGetResponseList
            );

            // 파트너 계정에도 리스트 최신화 보냄.
            messagingTemplate.convertAndSend(
                    "/sub/chat/roomlist/" + partnerId, chatRoomListGetResponseListPartner
            );

        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }
}