package org.hung.kstream.stockquotekstream.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Data;

@Data
public abstract class QuoteFeed {

    private String code;

    @JsonFormat(shape = Shape.STRING)
    private Instant timestamp;
}
