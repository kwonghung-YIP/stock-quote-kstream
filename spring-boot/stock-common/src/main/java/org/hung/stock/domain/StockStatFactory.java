package org.hung.stock.domain;

import java.time.Instant;
import java.util.random.RandomGenerator;

import lombok.Getter;
import lombok.Setter;

@Getter
public class StockStatFactory {
    
    @Setter
    private String code;
    
    private double initPrice; //S0
    
    private long avgVolume;
    
    @Setter
    private double mu; //annual
    
    @Setter
    private double sigma; //annual
    
    @Setter
    private Interval interval;
    
    private double nextPrice; //St
    private double high;
    private double low;
    private long nextVolume;
    private long t_price;
    private long t_volume;

    private RandomGenerator randGen = RandomGenerator.getDefault();

    public StockStatFactory() {
        reset();
    }

    public StockStatFactory(String code, double initPrice, long avgVolume, double mu, double sigma) {
        this(code,initPrice,avgVolume,mu,sigma,Interval.SECONDLY);
    }

    public StockStatFactory(String code, double initPrice, long avgVolume, double mu, double sigma, Interval interval) {
        this.code = code;
        this.initPrice = initPrice;
        this.avgVolume = avgVolume;
        this.mu = mu;
        this.sigma = sigma;
        this.interval = interval;
        reset();
    }

    public void reset() {
        this.nextPrice = initPrice;
        this.high = initPrice;
        this.low = initPrice;
        this.nextVolume = 0l;
        this.t_price = 0l;
        this.t_volume = 0l;
    }

    public void setInitPrice(double initPrice) {
        this.initPrice = initPrice;
        this.nextPrice = initPrice;
        this.high = initPrice;
        this.low = initPrice;
        this.t_price = 0;
    }

    public void setAvgVolume(long avgVolume) {
        this.avgVolume = avgVolume;
        this.nextVolume = 0;
        this.t_volume = 0;
    }

    private double calcNextPrice() {
        nextPrice += mu * interval.delta_t * nextPrice + sigma * Math.sqrt(interval.delta_t) * nextPrice * randGen.nextGaussian();
        high = Math.max(high,nextPrice);
        low = Math.min(low,nextPrice);
        t_price++;
        
        return nextPrice;
    }

    private long calcNextVolume() {
        if (!interval.isIntraDay()) {
            nextVolume = Math.round(avgVolume * randGen.nextDouble(0.95, 1.05));    
        } else {
            nextVolume += (avgVolume * randGen.nextDouble(0.95, 1.05))/interval.n;
        }
        t_volume++;

        return nextVolume;
    }

    public PriceFeed nextPriceFeed() {
        PriceFeed feed = new PriceFeed();
        feed.setCode(code);
        feed.setPrice(calcNextPrice());
        feed.setTimestamp(Instant.now());
        return feed;
    }

    public VolumeFeed nextVolumeFeed() {
        VolumeFeed feed = new VolumeFeed();
        feed.setCode(code);
        feed.setVolume(calcNextVolume());
        feed.setTimestamp(Instant.now());
        return feed;
    }

    public Quote nextQuote() {
        Quote quote = new Quote();
        quote.setCode(code);
        quote.setOpen(initPrice);
        quote.setPrice(calcNextPrice());
        quote.setHigh(high);
        quote.setLow(low);
        quote.setClose(nextPrice);
        quote.setChangePct((float)((nextPrice-initPrice)/initPrice)*100f);
        quote.setVolume(calcNextVolume());
        quote.setVer(t_price);
        quote.setTimestamp(Instant.now());
        return quote;
    }

    public enum Interval {
        MONTHLY(12),
        WEEKLY(52),
        DAILY(252),
        HOURLY(252*8),
        MINUTELY(252*8*60),
        SECONDLY(252*8*60*60);

        private final int n; // no of interval in years e.g. 12 for monthly
        private final double delta_t; // length of year per interval e.g. 1/12 for monthly

        private Interval(int n) {
            this.n = n;
            this.delta_t = 1d/n;
        }

        boolean isIntraDay() {
            return this.equals(HOURLY)||this.equals(MINUTELY)||this.equals(SECONDLY);
        }
    }
}
