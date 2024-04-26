package org.hung.kstream.stockquotekstream.kafka;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;
import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;
import static org.hung.kstream.stockquotekstream.kafka.QuoteFeedsConsolidatorSupplier.QUOTE_STORE;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.hung.kstream.stockquotekstream.domain.Quote;
import org.hung.kstream.stockquotekstream.webflux.QuoteRestWebClient;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuoteService {

    private final StreamsBuilderFactoryBean factoryBean;

    public Mono<Quote> getQuoteByCode(String code) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String,Quote> store = kafkaStreams.store(fromNameAndType(QUOTE_STORE,keyValueStore()));

        KeyQueryMetadata metadata = kafkaStreams.queryMetadataForKey(QUOTE_STORE, code, new StringSerializer());
        if (metadata.equals(KeyQueryMetadata.NOT_AVAILABLE)) {
            log.info("KeyQueryMetadata is not available for store:{} code:{}",QUOTE_STORE,code);
            return Mono.empty();
        } else {
            HostInfo activeHost = metadata.activeHost();
            log.info("activeHost for store:{} code:{} is {}",QUOTE_STORE,code,activeHost);
            if (!isThisHost(activeHost)) {
                QuoteRestWebClient webClient = getWebClient(activeHost);
                return webClient.getQuoteByCode(code);
            } else {
                return Mono.justOrEmpty(store.get(code));
            }
        }
    }

    public Flux<Quote> getAllQuote() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String,Quote> store = kafkaStreams.store(fromNameAndType(QUOTE_STORE,keyValueStore()));

        return kafkaStreams.streamsMetadataForStore(QUOTE_STORE).stream()
            .<Flux<Quote>>map(metadata -> {
                if (!isThisHost(metadata.hostInfo())) {
                    QuoteRestWebClient webClient = getWebClient(metadata.hostInfo());
                    return webClient.getAllQuote();
                } else {
                    return Flux.<Quote,KeyValueIterator<String,Quote>>generate(
                        () -> store.all(),
                        (iterator,sink) -> {
                            if (iterator.hasNext()) {
                                KeyValue<String,Quote> keyValue = iterator.next();
                                sink.next(keyValue.value);
                            } else {
                                sink.complete();
                            }
                            return iterator;
                        },
                        iterator -> iterator.close());
                }
            })
            .reduce(Flux.empty(), (agg,item) -> {
                return agg.concatWith(item);
            });
    }

    private boolean isThisHost(HostInfo info) {
        try {
            String localhost = InetAddress.getLocalHost().getHostName();
            log.info("localhost name is {}",localhost);
            return localhost.equals(info.host());
        } catch (UnknownHostException e) {
            log.error("failed to get localhostName", e);
            return false;
        }
    }

    private QuoteRestWebClient getWebClient(HostInfo hostInfo) {
        WebClient client = WebClient.builder()
            .baseUrl("http://%1s:%2d".formatted(hostInfo.host(),hostInfo.port()))
            .build();

        WebClientAdapter adapter = WebClientAdapter.create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        QuoteRestWebClient service = factory.createClient(QuoteRestWebClient.class);

        return service;
    }
}
