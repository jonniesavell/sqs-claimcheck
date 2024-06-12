package com.indigententerprises.sqsclaimcheck.configuration;

import com.indigententerprises.sqsclaimcheck.factories.MessageSenderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfiguration {

    @Value("${bucket.name}")
    private String bucketName;

    @Bean
    public MessageSenderFactory messageSenderFactory() {
        return new MessageSenderFactory(bucketName);
    }
}
