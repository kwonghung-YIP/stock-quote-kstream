package org.hung.kstream.stockquotekstream.kafka;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Set;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.hung.kstream.stockquotekstream.domain.PriceFeed;
import org.hung.kstream.stockquotekstream.domain.Quote;
import org.hung.kstream.stockquotekstream.domain.QuoteFeed;
import org.hung.kstream.stockquotekstream.domain.VolumeFeed;
import org.springframework.kafka.support.serializer.JsonSerde;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QuoteFeedsConsolidatorSupplier implements ProcessorSupplier<String, QuoteFeed, String, Quote> {
    
    static public final String QUOTE_STORE = "quote";

    @Override
    public Processor<String, QuoteFeed, String, Quote> get() {
        return new QuoteFeedsConsolidator();
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(QUOTE_STORE), 
                Serdes.String(),
                new JsonSerde<>(Quote.class))
        );
    }

    class QuoteFeedsConsolidator implements Processor<String, QuoteFeed, String, Quote> {

        private ProcessorContext<String, Quote> context;
        private KeyValueStore<String, Quote> store;

        @Override
        public void init(ProcessorContext<String, Quote> context) {
            this.context = context;
            this.store = context.getStateStore(QUOTE_STORE);
        }

        @Override
        public void process(Record<String, QuoteFeed> record) {
            QuoteFeed quoteFeed = record.value();

            String code = record.key();

            log.info("received quoteFeed {} with key {}", quoteFeed, code);
 
            Quote quote = this.store.get(code);
            
            if (quote == null) {
                log.info("ticket {} not found in Quote StateStore", code);
                quote = new Quote();
                quote.setCode(code);
            }

            if (quoteFeed instanceof PriceFeed priceFeed) {
                BigDecimal price = new BigDecimal(priceFeed.getPrice()).setScale(6,RoundingMode.HALF_UP);

                if (quote.getOpen()==null) {
                    quote.setOpen(price);
                    quote.setLow(price);
                    quote.setHigh(price);
                    quote.setPrice(price);
                    quote.setChangePct(0f);
                } else {
                    float open = quote.getOpen().floatValue();
                    quote.setLow(quote.getLow().min(price));
                    quote.setHigh(quote.getHigh().max(price));
                    quote.setPrice(price);
                    quote.setChangePct((price.floatValue()-open)/open*100);
                }
                quote.setVer(quote.getVer()+1);
                quote.setTimestamp(Instant.now());
                store.put(code, quote);
        
                log.info("forward quote {} to downstream", quote);
                context.forward(new Record<>(code,quote,Instant.now().toEpochMilli()));
                return;
            }

            if (quoteFeed instanceof VolumeFeed volumeFeed) {
                BigInteger volume = BigInteger.valueOf(volumeFeed.getVolume());

                quote.setVolume(volume);
                quote.setVer(quote.getVer()+1);
                quote.setTimestamp(Instant.now());
                store.put(code, quote);
        
                log.info("forward quote {} to downstream", quote);
                context.forward(new Record<>(code,quote,Instant.now().toEpochMilli()));
                return;
            }
            return;
        }

        @Override
        public void close() {
        }
    }

}
