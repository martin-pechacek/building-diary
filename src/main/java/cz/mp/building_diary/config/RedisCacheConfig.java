package cz.mp.building_diary.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@AllArgsConstructor
public class RedisCacheConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .prefixCacheNameWith("building-diary::")
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJacksonJsonRedisSerializer(objectMapper)
                        )
                );
    }
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration baseConfig) {

        return builder -> builder
                .withCacheConfiguration("projects",
                        baseConfig.entryTtl(Duration.ofMinutes(120)))
                .withCacheConfiguration("diaryEntries",
                        baseConfig.entryTtl(Duration.ofMinutes(240)));
    }

}
