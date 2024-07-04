package org.hung.kstream.web;

import org.hung.kstream.service.QuoteService;
import org.hung.stock.domain.Quote;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/quote")
public class QuoteRestController {
    
    private final QuoteService quoteService;

    @GetMapping("/find/{code}")
    public Mono<Quote> getQuote(@PathVariable String code) {
        return quoteService.getQuoteByCode(code);
    }

    @GetMapping("/all")
    public Flux<Quote> getAllQuote() {
        return quoteService.getAllQuote();
    }

    @GetMapping("/own")
    public Flux<Quote> getOwnQuote() {
        return quoteService.getOwnQuote();
    }

    @GetMapping("/most-actives")
    public Flux<Quote> mostActives(@RequestParam(name = "n", defaultValue = "5") int topN) {
        return quoteService.mostActive(topN);
    }

    @GetMapping("/gainers")
    public Flux<Quote> gainers(@RequestParam(name = "n", defaultValue = "5") int topN) {
        return quoteService.gainers(topN);
    }

    @GetMapping("/losers")
    public Flux<Quote> losers(@RequestParam(name = "n", defaultValue = "5") int topN) {
        return quoteService.losers(topN);
    }
}
