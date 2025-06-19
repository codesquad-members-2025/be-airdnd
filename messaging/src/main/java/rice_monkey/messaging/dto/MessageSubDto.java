package rice_monkey.messaging.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MessageSubDto {
    private Long userId;
    private Long partnerId;
    private ChatMessageDto chatMessageDto;
    private List<ChatRoomListGetResponse> list;
    private List<ChatRoomListGetResponse> partnerList;
}
