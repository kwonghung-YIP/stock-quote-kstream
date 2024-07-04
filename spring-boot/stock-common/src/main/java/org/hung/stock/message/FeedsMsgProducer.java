package org.hung.stock.message;

import java.util.function.Supplier;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.hung.stock.domain.PriceFeed;
import org.hung.stock.domain.StockStatFactory;
import org.hung.stock.domain.VolumeFeed;
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
@Profile("feeds-msg-producer")
public class FeedsMsgProducer {

    @Value("${feeds-msg-producer.price-feed.topic}")
    private String priceFeedTopicName;

    @Value("${feeds-msg-producer.volume-feed.topic}")
    private String volumeFeedTopicName;

    private final KafkaTemplate<String,PriceFeed> priceFeedTemplate;
    private final KafkaTemplate<String,VolumeFeed> volumeFeedTemplate;

    @Qualifier("stockPicker")
    private final Supplier<StockStatFactory> stockPicker;

    @Scheduled(fixedDelayString = "${feeds-msg-producer.price-feed.fixed-delay}", 
        initialDelayString = "${feeds-msg-producer.price-feed.initial-delay}")
    public void genPriceFeed() {
        StockStatFactory stock = stockPicker.get();
        log.info("Sending priceFeed to Kafka:{}",stock.getCode());
        PriceFeed feed = stock.nextPriceFeed();

        ProducerRecord<String,PriceFeed> record = new ProducerRecord<>(priceFeedTopicName,feed.getCode(),feed);
        this.priceFeedTemplate.send(record);
    }

    @Scheduled(fixedDelayString = "${feeds-msg-producer.volume-feed.fixed-delay}", 
        initialDelayString = "${feeds-msg-producer.volume-feed.initial-delay}")
    public void genVolumeFeed() {
        StockStatFactory stock = stockPicker.get();
        log.info("Sending volumeFeed to Kafka:{}",stock.getCode());
        VolumeFeed feed = stock.nextVolumeFeed();
        
        ProducerRecord<String,VolumeFeed> record = new ProducerRecord<>(volumeFeedTopicName,feed.getCode(),feed);
        this.volumeFeedTemplate.send(record);
    }
}
