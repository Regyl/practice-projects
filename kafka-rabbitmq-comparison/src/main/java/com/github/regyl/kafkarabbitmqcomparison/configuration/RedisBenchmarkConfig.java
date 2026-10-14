package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.annotation.RedisMqEnabled;
import com.github.regyl.kafkarabbitmqcomparison.model.AuthCodeMessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.data.redis.autoconfigure.RedisMessageListenerContainerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.annotation.EnableRedisListeners;
import org.springframework.data.redis.config.RedisListenerConfigurer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisMessageConverters;
import org.springframework.data.redis.serializer.SerializerMessageConverter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.MimeTypeUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RedisMqEnabled
@EnableRedisListeners
public class RedisBenchmarkConfig {

    @Bean
    JacksonJsonRedisSerializer<AuthCodeMessageModel> authCodeRedisSerializer(JsonMapper jsonMapper) {
        return new JacksonJsonRedisSerializer<>(jsonMapper, AuthCodeMessageModel.class);
    }

    @Bean
    RedisTemplate<String, AuthCodeMessageModel> authCodeRedisTemplate(
            RedisConnectionFactory connectionFactory,
            JacksonJsonRedisSerializer<AuthCodeMessageModel> authCodeRedisSerializer
    ) {
        RedisTemplate<String, AuthCodeMessageModel> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(authCodeRedisSerializer);
        template.setHashValueSerializer(authCodeRedisSerializer);
        return template;
    }

    @Bean(destroyMethod = "shutdown")
    ExecutorService redisListenerExecutor(@Value("${benchmark.listener-concurrency}") int concurrency) {
        AtomicInteger threadIndex = new AtomicInteger();
        return Executors.newFixedThreadPool(concurrency, runnable -> {
            Thread thread = new Thread(runnable, "redis-listener-" + threadIndex.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisMessageListenerContainerConfigurer configurer,
            ExecutorService redisListenerExecutor
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        configurer.configure(container, connectionFactory);
        container.setTaskExecutor(redisListenerExecutor);
        return container;
    }

    @Bean
    RedisListenerConfigurer authCodeRedisListenerConfigurer(
            JacksonJsonRedisSerializer<AuthCodeMessageModel> authCodeRedisSerializer
    ) {
        return new AuthCodeRedisListenerConfigurer(authCodeRedisSerializer);
    }

    private static final class AuthCodeRedisListenerConfigurer implements RedisListenerConfigurer {

        private final JacksonJsonRedisSerializer<AuthCodeMessageModel> serializer;

        private AuthCodeRedisListenerConfigurer(JacksonJsonRedisSerializer<AuthCodeMessageModel> serializer) {
            this.serializer = serializer;
        }

        @Override
        public void configureMessageConverters(RedisMessageConverters.Builder builder) {
            builder.addCustomConverter(new SerializerMessageConverter(serializer, MimeTypeUtils.APPLICATION_JSON));
        }
    }
}
