package rice_monkey.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rice_monkey.messaging.dto.ChatMessageDto;
import rice_monkey.messaging.dto.ChatRoomListGetResponse;
import rice_monkey.messaging.dto.MessageSubDto;
import rice_monkey.messaging.dto.MessageType;
import rice_monkey.messaging.redis.RedisPublisher;
import rice_monkey.messaging.repository.ChatRoomRedisRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final ChatRoomService chatRoomService;

    /**
     * 채팅방에 메시지 발송
     */
    public void sendChatMessage(ChatMessageDto chatMessage, String accessToken) {
        // 0. redis에 해당 채팅방roomId(key)에 마지막 메세지(value)를 넣어준다.
        chatRoomRedisRepository.setLastChatMessage(chatMessage.getRoomId(), chatMessage);

        Long userId = chatMessage.getUserId();
        Long partnerId;

        // 1. 채팅방이 삭제되는 것이라면 delete 를 해준다.
        if (chatMessage.getType().equals(MessageType.DELETE)) {
            chatRoomService.deleteChatRoom(accessToken, chatMessage.getRoomId(), userId);
            chatRoomRedisRepository.deleteChatRoom(userId,chatMessage.getRoomId());
        }

        ChatRoomListGetResponse newChatRoomList = null;
        if (chatRoomRedisRepository.existChatRoom(userId, chatMessage.getRoomId())) {
            newChatRoomList = chatRoomRedisRepository.getChatRoom(userId, chatMessage.getRoomId());
        } else {
            newChatRoomList = chatRoomService.getChatRoomInfo(accessToken, chatMessage.getRoomId());
        }

        partnerId = getPartnerId(chatMessage, newChatRoomList);

        // 2. 채팅방 리스트에 새로운 채팅방 정보가 없다면, 넣어준다. 마지막 메시지도 같이 담는다. 상대방 레디스에도 업데이트 해준다.
        setNewChatRoomInfo(chatMessage, newChatRoomList);

        // 3. 마지막 메시지들이 담긴 채팅방 리스트들을 가져온다.
        List<ChatRoomListGetResponse> chatRoomListGetResponseList = chatRoomService.getChatRoomList(userId, accessToken);
        // 4. 파트너 채팅방 리스트도 가져온다. (파트너는 userId 로만)
        List<ChatRoomListGetResponse> partnerChatRoomGetResponseList = getChatRoomListByUserId(partnerId);

        // 5. 마지막 메세지 기준으로 정렬 채팅방 리스트 정렬
        chatRoomListGetResponseList = chatRoomService.sortChatRoomListLatest(chatRoomListGetResponseList);
        partnerChatRoomGetResponseList = chatRoomService.sortChatRoomListLatest(partnerChatRoomGetResponseList);

        MessageSubDto messageSubDto = MessageSubDto.builder()
                .userId(userId)
                .partnerId(partnerId)
                .chatMessageDto(chatMessage)
                .list(chatRoomListGetResponseList)
                .partnerList(partnerChatRoomGetResponseList)
                .build();

        redisPublisher.publish(messageSubDto);
    }

    private Long getPartnerId(ChatMessageDto chatMessage, ChatRoomListGetResponse chatRoomInfo) {
        Long userId = chatMessage.getUserId();
        Long partnerId = chatRoomInfo.getPartnerId();

        if (partnerId == null || partnerId.equals(userId)) {
            throw new IllegalStateException("잘못된 파트너 ID입니다.");
        }
        return partnerId;
    }

    private void setNewChatRoomInfo(ChatMessageDto chatMessage, ChatRoomListGetResponse chatRoomInfo) {
        Long userId = chatMessage.getUserId();
        Long roomId = chatMessage.getRoomId();

        // 마지막 메시지 업데이트
        chatRoomInfo.setLastMessage(chatMessage.getMessage());
        // sentTime은 문자열로 들어있을 수 있으니 적절히 파싱 또는 현재 시각을 사용
        chatRoomInfo.setLastMessageTime(java.time.LocalDateTime.now());

        // 나의 Redis에 저장
        chatRoomRedisRepository.saveChatRoom(userId, roomId, chatRoomInfo);

        // 상대방도 동일하게 저장
        Long partnerId = getPartnerId(chatMessage, chatRoomInfo);
        chatRoomRedisRepository.saveChatRoom(partnerId, roomId, chatRoomInfo);
    }

    private List<ChatRoomListGetResponse> getChatRoomListByUserId(Long userId) {
        return chatRoomRedisRepository.getChatRoomList(userId);
    }




}
