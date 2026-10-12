package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.model.MessagingNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitBenchmarkConfig {

    @Bean
    DirectExchange authExchange() {
        return new DirectExchange(MessagingNames.EXCHANGE, true, false);
    }

    @Bean
    Queue authQueue() {
        return QueueBuilder.durable(MessagingNames.QUEUE).build();
    }

    @Bean
    Binding authBinding(Queue authQueue, DirectExchange authExchange) {
        return BindingBuilder.bind(authQueue).to(authExchange).with(MessagingNames.ROUTING_KEY);
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
