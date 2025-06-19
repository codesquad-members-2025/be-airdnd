package rice_monkey.messaging.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import rice_monkey.messaging.dto.ChatMessageDto;
import rice_monkey.messaging.service.ChatMongoService;
import rice_monkey.messaging.service.ChatService;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatMongoService chatMongoService;
    private final ChatService chatService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message,
                        @Header("Authorization") String accessToken
    ) {
        ChatMessageDto chatMessageDto = chatMongoService.save(message);
        chatService.sendChatMessage(chatMessageDto, accessToken); //RedisPublisher 호출
    }

}
