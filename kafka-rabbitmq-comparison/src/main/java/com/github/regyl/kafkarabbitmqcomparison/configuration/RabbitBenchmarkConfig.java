package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.annotation.RabbitMqEnabled;
import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNamesConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RabbitMqEnabled
public class RabbitBenchmarkConfig {

    @Bean
    DirectExchange authExchange() {
        return new DirectExchange(MessagingNamesConstant.EXCHANGE, true, false);
    }

    @Bean
    Queue authQueue() {
        return QueueBuilder.durable(MessagingNamesConstant.QUEUE).build();
    }

    @Bean
    Binding authBinding(Queue authQueue, DirectExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with(MessagingNamesConstant.ROUTING_KEY);
    }

    @Bean
    JacksonJsonMessageConverter jacksonJsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper, "com.github.regyl.kafkarabbitmqcomparison");
    }

    @Bean
    RabbitTemplateCustomizer persistentMessages() {
        return template -> template.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }
}
