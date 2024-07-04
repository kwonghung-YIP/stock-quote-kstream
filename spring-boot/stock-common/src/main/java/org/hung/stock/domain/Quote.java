package org.hung.stock.domain;

import java.time.Instant;

import lombok.Data;

@Data
public class Quote {

    private String code;
    private Double price;
    private Double open;
    private Double low;
    private Double high;
    private Double close;
    private Long volume;
    private Float changePct;
    private Instant timestamp;
    private long ver;
}
