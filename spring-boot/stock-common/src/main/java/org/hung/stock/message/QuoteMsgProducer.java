package org.hung.stock.message;

import java.util.function.Supplier;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.hung.stock.domain.Quote;
import org.hung.stock.domain.StockStatFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@Component
@Profile("quote-msg-producer")
public class QuoteMsgProducer {
    
    @Value("${quote-msg-producer.quote.topic}")
    private String quoteTopicName;

    private final KafkaTemplate<String,Quote> quoteTemplate;

        @Qualifier("stockPicker")
    private final Supplier<StockStatFactory> stockPicker;

    @Scheduled(fixedDelayString = "${quote-msg-producer.quote.fixed-delay}", 
        initialDelayString = "${quote-msg-producer.quote.initial-delay}")
    public void genPriceFeed() {
        StockStatFactory stock = stockPicker.get();
        log.info("Sending quote to Kafka:{}",stock.getCode());
        Quote quote = stock.nextQuote();

        ProducerRecord<String,Quote> record = new ProducerRecord<>(quoteTopicName,quote.getCode(),quote);
        this.quoteTemplate.send(record);
    }
}
