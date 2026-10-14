package com.github.regyl.kafkarabbitmqcomparison.configuration;

import com.github.regyl.kafkarabbitmqcomparison.annotation.ActiveMqEnabled;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Session;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jms.ConnectionFactoryUnwrapper;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.JacksonJsonMessageConverter;
import org.springframework.jms.support.converter.MessageType;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@ActiveMqEnabled
public class ActiveMqBenchmarkConfig {

    private static final String TYPE_ID_PROPERTY = "_type";

    @Bean
    JacksonJsonMessageConverter jmsJacksonJsonMessageConverter(JsonMapper jsonMapper) {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY);
        converter.setTrustedPackages("com.github.regyl.kafkarabbitmqcomparison.model");
        return converter;
    }

    @Bean
    DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, ConnectionFactoryUnwrapper.unwrapCaching(connectionFactory));
        factory.setSessionTransacted(false);
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }

    @Bean
    SmartInitializingSingleton persistentJmsMessages(ObjectProvider<JmsTemplate> jmsTemplate) {
        return () -> jmsTemplate.ifAvailable(template -> {
            template.setExplicitQosEnabled(true);
            template.setDeliveryMode(DeliveryMode.PERSISTENT);
        });
    }
}
