package org.hung.kstream.stockquotekstream.webflux;

import org.hung.kstream.stockquotekstream.domain.Quote;
import org.hung.kstream.stockquotekstream.kafka.QuoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/quote")
public class QuoteRestController {
    
    private final QuoteService quoteService; 

    @GetMapping("/{code}")
    public Mono<Quote> getQuote(@PathVariable String code) {
        return quoteService.getQuoteByCode(code);
    }

    @GetMapping("/")
    public Flux<Quote> getAllQuote() {
        return quoteService.getAllQuote();
    }
    

}
