package org.hung.kstream.service;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;
import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;
import static org.hung.kstream.kafka.FeedsConsolidatorSupplier.QUOTE_STORE_NAME;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.hung.kstream.web.QuoteRestWebClient;
import org.hung.stock.domain.Quote;
import org.springframework.beans.factory.annotation.Value;
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
@Service
@RequiredArgsConstructor
public class QuoteService {

    private static final Comparator<Quote> volComparator = 
        (q1,q2) -> {
            Long vol1 = q1.getVolume()!=null?q1.getVolume():0l;
            Long vol2 = q2.getVolume()!=null?q2.getVolume():0l;
            return Float.compare(vol1,vol2);
        };

    private static final Comparator<Quote> chgPctComparator = 
        (q1,q2) -> {
            Float chgPct1 = q1.getChangePct()!=null?q1.getChangePct():0f;
            Float chgPct2 = q2.getChangePct()!=null?q2.getChangePct():0f;
            return Float.compare(chgPct1,chgPct2);
        };
        
    private final StreamsBuilderFactoryBean factoryBean;

    @Value("${spring.security.user.name}")
    private String defaultUser;

    @Value("${spring.security.user.password}")
    private String defaultPasswd;

    public Mono<Quote> getQuoteByCode(String code) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String,Quote> store = kafkaStreams.store(fromNameAndType(QUOTE_STORE_NAME,keyValueStore()));

        KeyQueryMetadata metadata = kafkaStreams.queryMetadataForKey(QUOTE_STORE_NAME, code, new StringSerializer());
        if (metadata.equals(KeyQueryMetadata.NOT_AVAILABLE)) {
            log.info("KeyQueryMetadata is not available for store:{} code:{}",QUOTE_STORE_NAME,code);
            return Mono.empty();
        } else {
            HostInfo activeHost = metadata.activeHost();
            log.info("activeHost for store:{} code:{} is {}",QUOTE_STORE_NAME,code,activeHost);
            if (!isLocalHost(activeHost)) {
                log.info("sending request to activeHost {}",activeHost);
                QuoteRestWebClient webClient = getWebClient(activeHost);
                return webClient.getQuoteByCode(code);
            } else {
                log.info("get quote {} in this host {}",code,activeHost);
                return Mono.justOrEmpty(store.get(code));
            }
        }
    }

    public Flux<Quote> getAllQuote() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();

        return kafkaStreams.streamsMetadataForStore(QUOTE_STORE_NAME).stream()
            .<Flux<Quote>>map(metadata -> {
                log.info("metadata:{}",metadata);
                if (!isLocalHost(metadata.hostInfo())) {
                    log.info("Get quotes from other node:{}",metadata.hostInfo());
                    QuoteRestWebClient webClient = getWebClient(metadata.hostInfo());
                    return webClient.getOwnQuote();
                } else {
                    log.info("Get quotes from local:{}",metadata.hostInfo());
                    return this.getOwnQuote();
                }
            })
            .reduce(Flux.empty(), (agg,item) -> {
                return agg.concatWith(item);
            });
    }

    public Flux<Quote> mostActive(int topN) {
        return getAllQuote()
            .collectList()
            .map(list -> {
                list.sort(volComparator);
                return list.subList(Math.max(0,list.size()-topN),list.size()).reversed();
            })
            .flatMapIterable(list -> list);
    }
    
    public Flux<Quote> gainers(int topN) {
        return getAllQuote()
            .collectList()
            .map(list -> {
                list.sort(chgPctComparator);
                return list.subList(Math.max(0,list.size()-topN),list.size()).reversed();
            })
            .flatMapIterable(list -> list);
    }

    public Flux<Quote> losers(int topN) {
        return getAllQuote()
            .collectList()
            .map(list -> {
                list.sort(chgPctComparator);
                return list.subList(0,Math.min(topN,list.size()));
            })
            .flatMapIterable(list -> list);
    }

    public Flux<Quote> getOwnQuote() {
        KafkaStreams KafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String,Quote> store = KafkaStreams.store(fromNameAndType(QUOTE_STORE_NAME, keyValueStore()));

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

    private boolean isLocalHost(HostInfo info) {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            log.info("localhost.hostName:{}",localhost.getHostName());
            log.info("localhost.hostAddress:{}",localhost.getHostAddress());
            return localhost.getHostAddress().equals(info.host());
        } catch (UnknownHostException e) {
            log.error("failed to get localhostName", e);
            return false;
        }
    }

    private QuoteRestWebClient getWebClient(HostInfo hostInfo) {
        WebClient client = WebClient.builder()
            .defaultHeaders(headers -> headers.setBasicAuth(defaultUser, defaultPasswd))
            .baseUrl("http://%1s:%2d".formatted(hostInfo.host(),hostInfo.port()))
            .build();

        WebClientAdapter adapter = WebClientAdapter.create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        QuoteRestWebClient service = factory.createClient(QuoteRestWebClient.class);

        return service;
    }
}
