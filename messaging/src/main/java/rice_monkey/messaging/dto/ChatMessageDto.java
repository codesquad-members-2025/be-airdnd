package rice_monkey.messaging.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    private MessageType type;   // 메시지 타입 (ENTER, TALK, QUIT, NEGO_REQ, NEGO_ALLOW, NEGO_DENIED, DELETE)
    private Long roomId;      // 방 번호
    private Long userId;        // 사용자 id
    private String message;     // 메시지
    private LocalDateTime time;        // 전송 시간
    private long userCount;     // 채팅방 인원 수
}

