package org.hung.stock;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.hung.stock.domain.PriceFeed;
import org.hung.stock.domain.Quote;
import org.hung.stock.domain.StockStatFactory;
import org.hung.stock.domain.VolumeFeed;
import org.junit.jupiter.api.Test;

public class StockStatFactoryTest {
    
    @Test
    void genNextFeedPojo() {
        StockStatFactory factory = new StockStatFactory("ABC", 99.99, 12341234, 0.07, 0.15);
        
        PriceFeed priceFeed = factory.nextPriceFeed();
        assertThat(priceFeed.getCode(), is(factory.getCode()));
        assertThat((priceFeed.getPrice()-factory.getInitPrice())/factory.getNextPrice(), closeTo(0, 0.05));

        VolumeFeed volumeFeed1 = factory.nextVolumeFeed();
        assertThat(volumeFeed1.getCode(), is(factory.getCode()));
        assertThat(volumeFeed1.getVolume(), is(greaterThan(0l)));

        VolumeFeed volumeFeed2 = factory.nextVolumeFeed();
        assertThat(volumeFeed2.getCode(), is(factory.getCode()));
        assertThat(volumeFeed2.getVolume(), is(greaterThanOrEqualTo(volumeFeed1.getVolume())));

        factory.reset();
        Quote quote = factory.nextQuote();
        assertThat(quote.getCode(), is(factory.getCode()));
    }
}
