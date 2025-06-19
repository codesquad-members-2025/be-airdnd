package rice_monkey.messaging.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListGetResponse {

    private Long roomId;
    private Long partnerId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private String roomName;

    public static ChatRoomListGetResponse of(Long roomId) {
        return ChatRoomListGetResponse.builder()
                .roomId(roomId)
                .roomName("기본 채팅방 이름")
                .lastMessage("메시지가 없습니다")
                .lastMessageTime(LocalDateTime.now())
                .build();
    }
}
