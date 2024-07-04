package org.hung.stock.domain;

import java.time.Instant;

import org.apache.kafka.common.header.Headers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Data;

@Data
public abstract class QuoteFeed {

    /*public static JavaType resolveJavaType(String topic, byte[] data, Headers headers) {
        if ("price_feed".equals(topic)) {
            return TypeFactory.defaultInstance().constructType(PriceFeed.class);
        } else {
            return TypeFactory.defaultInstance().constructType(VolumeFeed.class);
        }
    }*/

    private String code;

    @JsonFormat(shape = Shape.STRING)
    private Instant timestamp;
}
