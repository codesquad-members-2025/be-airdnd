package rice_monkey.messaging.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rice_monkey.messaging.dto.ChatMessageDto;

import java.util.List;

public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDto, String> {

    List<ChatMessageDto> findByRoomIdOrderBySentTimeAsc(Long roomId);
}
