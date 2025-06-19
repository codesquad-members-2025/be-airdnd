package rice_monkey.messaging.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import rice_monkey.messaging.dto.ChatMessageDto;
import rice_monkey.messaging.dto.ChatRoomListGetResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class ChatRoomRedisRepository {

    private static final String CHAT_ROOM_KEY = "_CHAT_ROOM_RESPONSE_LIST";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "chatroom:";

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoomListGetResponse> opsHashChatRoom;

    private String getChatRoomKey(Long userId) {
        // redis에 채팅방 리스트 저장 시 사용될 key
        return userId + CHAT_ROOM_KEY;
    }

    public void initChatRoomList(Long userId, List<ChatRoomListGetResponse> list) {
        // 채팅방 리스트 초기화
        if (redisTemplate.hasKey(getChatRoomKey(userId))) {
            redisTemplate.delete(getChatRoomKey(userId));
        }

        opsHashChatRoom = redisTemplate.opsForHash();
        for (ChatRoomListGetResponse chatRoomListGetRes : list) {
            setChatRoom(userId, chatRoomListGetRes.getRoomId(), chatRoomListGetRes);
        }
    }

    public List<ChatRoomListGetResponse> getChatRoomList(Long userId) {
        // 채팅방 리스트 조회
        return objectMapper.convertValue(opsHashChatRoom.values(getChatRoomKey(userId)), new TypeReference<>() {});
    }

    public void setLastChatMessage(Long roomId, ChatMessageDto message) {
        try {
            String key = PREFIX + "last:" + roomId;
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ChatMessageDto", e);
        }
    }

    public void deleteChatRoom(Long userId, Long roomId) {
        String key = PREFIX + userId + ":" + roomId;
        redisTemplate.delete(key);
    }

    public boolean existChatRoom(Long userId, Long roomId) {
        String key = PREFIX + userId + ":" + roomId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public ChatRoomListGetResponse getChatRoom(Long userId, Long roomId) {
        String key = PREFIX + userId + ":" + roomId;
        String value = redisTemplate.opsForValue().get(key).toString();
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, ChatRoomListGetResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize ChatRoomListGetResponse", e);
        }
    }

    public void setChatRoom(Long userId, Long roomId, ChatRoomListGetResponse chatRoomInfo) {
        String key = getChatRoomKey(userId); // 예: "123_CHAT_ROOM_RESPONSE_LIST"
        String hashKey = roomId.toString(); // 각 채팅방의 ID를 hash key로 사용

        opsHashChatRoom.put(key, hashKey, chatRoomInfo);

        // 선택적으로 TTL 설정 (없으면 영구 저장됨)
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    public void saveChatRoom(Long userId, Long roomId, ChatRoomListGetResponse response) {
        String key = PREFIX + userId + ":" + roomId;
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response), 1, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ChatRoomListGetResponse", e);
        }
    }

}
