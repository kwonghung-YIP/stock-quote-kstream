package org.hung.kstream.stockquotekstream.kafka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.hung.kstream.stockquotekstream.domain.PriceFeed;
import org.hung.kstream.stockquotekstream.domain.QuoteFeedFactory;
import org.hung.kstream.stockquotekstream.domain.VolumeFeed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("quote-producer")
@EnableScheduling
public class FeedsProducerConfig {

    @Value("${quote-processor.kafka.topics.price-feed}")
    private String priceFeedTopicName;

    @Value("${quote-processor.kafka.topics.volume-feed}")
    private String volumeFeedTopicName;

    @Value("classpath:/init-quote.csv")
    private Resource initQuoteCsv;

    private RandomGenerator randGen;
    private List<QuoteFeedFactory> stocks = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        this.randGen = RandomGenerator.getDefault();

        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper();
        try (MappingIterator<QuoteFeedFactory> it = mapper.readerFor(QuoteFeedFactory.class).with(schema).readValues(initQuoteCsv.getFile())) {
            while (it.hasNextValue()) {
                QuoteFeedFactory factory = it.nextValue();
                factory.setNextPrice(factory.getInitPrice());
                factory.setInterval(QuoteFeedFactory.Interval.MINUTELY);
                stocks.add(factory);
            }
        } catch (IOException e) {
            log.error("Error when reading init-qoute.csv file", e);
            throw e;
        }
    }

    @Bean
    public NewTopic priceFeedTopic() {
        return TopicBuilder.name(priceFeedTopicName).build();
    }

    @Bean
    public NewTopic volumeFeedTopic() {
        return TopicBuilder.name(volumeFeedTopicName).build();
    }

    @Component
    @RequiredArgsConstructor
    class QuoteFeedProducer {
    
        private final KafkaTemplate<String,PriceFeed> priceFeedTemplate;
        private final KafkaTemplate<String,VolumeFeed> volumeFeedTemplate;

        @Scheduled(fixedDelayString = "2000", initialDelayString = "500")
        public void genPriceFeed() {
            log.info("Send priceFeed message...");
            PriceFeed feed = genRandomPriceFeed();
            ProducerRecord<String,PriceFeed> record = new ProducerRecord<>(priceFeedTopicName,feed.getCode(),feed);
            priceFeedTemplate.send(record);
        }

        @Scheduled(fixedDelayString = "2000", initialDelayString = "1000")
        public void genVolumeFeed() {
            log.info("Send volumeFeed message...");
            VolumeFeed feed = genRandomVolumeFeed();
            ProducerRecord<String,VolumeFeed> record = new ProducerRecord<>(volumeFeedTopicName,feed.getCode(),feed);
            volumeFeedTemplate.send(record);
        }
    }

    private PriceFeed genRandomPriceFeed() {
        QuoteFeedFactory factory = stocks.get(randGen.nextInt(stocks.size()));
        return factory.nextPriceFeed();
    }

    private VolumeFeed genRandomVolumeFeed() {
        QuoteFeedFactory factory = stocks.get(randGen.nextInt(stocks.size()));
        return factory.nextVolumeFeed();
    }
}
