package org.hung.kstream.stockquotekstream.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

import lombok.Data;

@Data
public class Quote {
    private String code;
    private BigDecimal price;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal close;
    private BigInteger volume;
    private float changePct;
    private Instant timestamp;
    private long ver;
}
