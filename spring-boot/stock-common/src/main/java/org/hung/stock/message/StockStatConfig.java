package org.hung.stock.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import org.hung.stock.domain.StockStatFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile({"feeds-msg-producer","quote-msg-producer"})
public class StockStatConfig {
    
    @Value("classpath:stock-stat.csv")
    private Resource stockStatCsv;
    
    @Bean
    public List<StockStatFactory> stockStatList() throws IOException {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        CsvMapper mapper = new CsvMapper();
        InputStream in = stockStatCsv.getInputStream();
        try (MappingIterator<StockStatFactory> it = mapper.readerFor(StockStatFactory.class).with(schema).readValues(in)) {
            List<StockStatFactory> result = new ArrayList<StockStatFactory>();
            while (it.hasNextValue()) {
                StockStatFactory factory = it.nextValue();
                factory.setInterval(StockStatFactory.Interval.MINUTELY);
                result.add(factory);
            }
            return result;
        } catch (IOException e) {
            log.error("Error when reading stock-stat.csv file", e);
            throw e;
        }
    }

    @Bean
    public Supplier<StockStatFactory> stockPicker(List<StockStatFactory> list) {
        RandomGenerator randGen = RandomGenerator.getDefault();
        return () -> list.get(randGen.nextInt(list.size()));
    }

}
