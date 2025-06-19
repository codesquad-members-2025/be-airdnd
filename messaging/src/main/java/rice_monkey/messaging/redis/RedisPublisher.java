package rice_monkey.messaging.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import rice_monkey.messaging.dto.MessageSubDto;

@RequiredArgsConstructor
@Service
public class RedisPublisher {
    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;

    public void publish(MessageSubDto message) {
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }
}

