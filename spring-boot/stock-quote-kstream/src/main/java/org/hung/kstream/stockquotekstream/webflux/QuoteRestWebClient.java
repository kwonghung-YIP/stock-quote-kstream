package org.hung.kstream.stockquotekstream.webflux;

import org.hung.kstream.stockquotekstream.domain.Quote;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange("/quote")
public interface QuoteRestWebClient {

    @GetExchange("/{code}")
    Mono<Quote> getQuoteByCode(@PathVariable String code);

    @GetExchange("/")
    Flux<Quote> getAllQuote();

}
