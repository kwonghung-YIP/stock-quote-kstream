package org.hung.kstream.kafka;

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
import org.hung.stock.domain.PriceFeed;
import org.hung.stock.domain.Quote;
import org.hung.stock.domain.QuoteFeed;
import org.hung.stock.domain.VolumeFeed;
import org.springframework.kafka.support.serializer.JsonSerde;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeedsConsolidatorSupplier implements ProcessorSupplier<String,QuoteFeed,String,Quote> {
    
    static public final String QUOTE_STORE_NAME = "quote";

    @Override
    public Processor<String, QuoteFeed, String, Quote> get() {
        return new FeedsConsolidator();
    }

    @Override
    public Set<StoreBuilder<?>> stores() {
        return Set.of(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(QUOTE_STORE_NAME), 
                Serdes.String(),
                new JsonSerde<>(Quote.class))
        );
    }

    class FeedsConsolidator implements Processor<String,QuoteFeed,String,Quote> {

        private ProcessorContext<String, Quote> context;
        private KeyValueStore<String,Quote> store;

        @Override
        public void init(ProcessorContext<String, Quote> context) {
            this.context = context;
            this.store = context.getStateStore(QUOTE_STORE_NAME);
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
                double price = priceFeed.getPrice();

                if (quote.getOpen()==null) {
                    quote.setOpen(price);
                    quote.setLow(price);
                    quote.setHigh(price);
                    quote.setPrice(price);
                    quote.setChangePct(0f);
                } else {
                    float open = quote.getOpen().floatValue();
                    quote.setLow(Math.min(price,quote.getLow()));
                    quote.setHigh(Math.max(price,quote.getHigh()));
                    quote.setPrice(price);
                    quote.setChangePct((float)(price-open)/open*100);
                }
                quote.setVer(quote.getVer()+1);
                quote.setTimestamp(Instant.now());
                store.put(code, quote);
        
                log.info("forward quote {} to downstream", quote);
                context.forward(new Record<>(code,quote,Instant.now().toEpochMilli()));
                return;
            }

            if (quoteFeed instanceof VolumeFeed volumeFeed) {
                quote.setVolume(volumeFeed.getVolume());
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
            // TODO Auto-generated method stub
            Processor.super.close();
        }

    }
}
