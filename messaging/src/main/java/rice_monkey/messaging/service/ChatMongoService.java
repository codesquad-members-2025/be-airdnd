package rice_monkey.messaging.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rice_monkey.messaging.dto.ChatMessageDto;
import rice_monkey.messaging.repository.ChatMessageMongoRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ChatMongoService {

    private final ChatMessageMongoRepository chatMessageMongoRepository;

    public ChatMessageDto save(ChatMessageDto dto) {
        ChatMessageDto message = ChatMessageDto.builder()
                .roomId(dto.getRoomId())
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .type(dto.getType())
                .time(LocalDateTime.now())
                .build();

        return chatMessageMongoRepository.save(message);
    }
}
