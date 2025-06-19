package rice_monkey.messaging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rice_monkey.messaging.dto.ChatRoomListGetResponse;
import rice_monkey.messaging.dto.ChatMessageDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    // 실제 구현에서는 DB나 외부 서비스에서 조회
    public ChatRoomListGetResponse getChatRoomInfo(String accessToken, Long roomId) {
        // accessToken으로 유저 인증 후 DB에서 조회
        return ChatRoomListGetResponse.of(roomId); // 예시
    }

    public List<ChatRoomListGetResponse> getChatRoomList(Long userId, String accessToken) {
        // DB에서 해당 유저의 채팅방 목록 조회
        return List.of(); // 더미
    }

    public List<ChatRoomListGetResponse> sortChatRoomListLatest(List<ChatRoomListGetResponse> list) {
        list.sort(Comparator.comparing(ChatRoomListGetResponse::getLastMessageTime).reversed());
        return list;
    }

    public void deleteChatRoom(String accessToken, Long roomId, Long userId) {
        // 인증된 사용자가 roomId 삭제 요청 → DB에서 삭제 처리
    }
}
