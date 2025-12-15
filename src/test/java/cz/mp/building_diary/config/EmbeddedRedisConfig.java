package cz.mp.building_diary.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {

	private RedisServer redisServer;

	@PostConstruct
	public void startRedis() throws IOException {
		redisServer = new RedisServer(6370);
		redisServer.start();
	}

	@PreDestroy
	public void stopRedis() throws IOException {
		if (redisServer != null && redisServer.isActive()) {
			redisServer.stop();
		}
	}

}