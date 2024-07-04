package org.hung.kafka.mqttsinkconnector.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("quote-msg-producer")
@EnableKafka
@EnableScheduling
public class QuoteProducerConfig {

    @Bean
    public NewTopic quoteTopic() {
        log.info("create topic {}","quote");
        return TopicBuilder.name("quote").partitions(10).build();
    }

    @Component
    @RequiredArgsConstructor
    class quoteMsgProducer {

        private final KafkaTemplate<String,String> quoteTemplate;

        @Scheduled(fixedDelayString = "PT1S")
        public void generateQuoteMessage() {
            quoteTemplate.send("quote","key", "message");
        }
    }
}
