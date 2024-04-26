package org.hung.kstream.stockquotekstream.domain;

import java.time.Instant;
import java.util.random.RandomGenerator;

import lombok.Getter;
import lombok.Setter;

@Getter
public class QuoteFeedFactory {

    @Setter
    private String code;
    @Setter
    private double initPrice; //S0
    @Setter
    private long avgVolume;
    @Setter
    private double mu; //annual
    @Setter
    private double sigma; //annual
    @Setter
    private Interval interval;
    @Setter
    private double nextPrice; //St
    private long nextVolume;
    private long t_price;
    private long t_volume;

    private RandomGenerator randGen = RandomGenerator.getDefault();

    public QuoteFeedFactory() {
        reset();
    }
    public QuoteFeedFactory(String code, double initPrice, long avgVolume, double mu, double sigma) {
        this(code,initPrice,avgVolume,mu,sigma,Interval.SECONDLY);
    }

    public QuoteFeedFactory(String code, double initPrice, long avgVolume, double mu, double sigma, Interval interval) {
        this.code = code;
        this.initPrice = initPrice;
        this.avgVolume = avgVolume;
        this.mu = mu;
        this.sigma = sigma;
        this.interval = interval;

        this.nextPrice = initPrice;
        reset();
    }

    public void reset() {
        this.nextVolume = 0l;
        this.t_price = 0l;
        this.t_volume = 0l;
    }

    public PriceFeed nextPriceFeed() {
        nextPrice += mu * interval.delta_t * nextPrice + sigma * Math.sqrt(interval.delta_t) * nextPrice * randGen.nextGaussian();
        t_price++;

        PriceFeed feed = new PriceFeed();
        feed.setCode(code);
        feed.setPrice(nextPrice);
        feed.setTimestamp(Instant.now());
        return feed;
    }

    public VolumeFeed nextVolumeFeed() {
        if (!interval.isIntraDay()) {
            nextVolume = Math.round(avgVolume * randGen.nextDouble(0.95, 1.05));    
        } else {
            nextVolume += (avgVolume * randGen.nextDouble(0.95, 1.05))/interval.n;
        }
        t_volume++;

        VolumeFeed feed = new VolumeFeed();
        feed.setCode(code);
        feed.setVolume(nextVolume);
        feed.setTimestamp(Instant.now());
        return feed;
    }

    public enum Interval {
        MONTHLY(12),
        WEEKLY(52),
        DAILY(252),
        HOURLY(252*8),
        MINUTELY(252*8*60),
        SECONDLY(252*8*60*60);

        private final int n;
        private final double delta_t;

        private Interval(int n) {
            this.n = n;
            this.delta_t = 1d/n;
        }

        boolean isIntraDay() {
            return this.equals(HOURLY)||this.equals(MINUTELY)||this.equals(SECONDLY);
        }
    }
}
