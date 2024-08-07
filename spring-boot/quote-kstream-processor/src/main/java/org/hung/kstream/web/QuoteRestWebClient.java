package org.hung.kstream.web;

import org.hung.stock.domain.Quote;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange("/quote")
public interface QuoteRestWebClient {

    @GetExchange("/find/{code}")
    Mono<Quote> getQuoteByCode(@PathVariable String code);

    @GetExchange("/all")
    Flux<Quote> getAllQuote();

    @GetExchange("/own")
    Flux<Quote> getOwnQuote();
}
