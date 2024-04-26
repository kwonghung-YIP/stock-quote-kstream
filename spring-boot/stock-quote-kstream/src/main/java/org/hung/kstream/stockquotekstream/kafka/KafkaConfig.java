package org.hung.kstream.stockquotekstream.kafka;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.hung.kstream.stockquotekstream.domain.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.support.serializer.JsonSerde;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    @Value("${quote-processor.kafka.topics.price-feed}")
    private String priceFeedTopic;

    @Value("${quote-processor.kafka.topics.volume-feed}")
    private String volumeFeedTopic;

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamBuilderFactoryConfigurer() {
        
        return new StreamsBuilderFactoryBeanConfigurer() {
            @Override
            public void configure(StreamsBuilderFactoryBean factoryBean) {
                factoryBean.setInfrastructureCustomizer(new MyKafkaStreamsInfrastructureCustomizer());//serdeConfig));
            }  
        };
    }

    @RequiredArgsConstructor
    class MyKafkaStreamsInfrastructureCustomizer implements KafkaStreamsInfrastructureCustomizer {
        
        @Override
        public void configureTopology(Topology topology) {

            Serde<Quote> quoteSerde = new JsonSerde<>(Quote.class);

            topology.addSource("quote-feeds", priceFeedTopic, volumeFeedTopic)
                .addProcessor("quote-consolidate", new QuoteFeedsConsolidatorSupplier(), "quote-feeds")
                .addSink("quote-update", "quote", Serdes.String().serializer(), quoteSerde.serializer(), "quote-consolidate");
        }        
    }
}
