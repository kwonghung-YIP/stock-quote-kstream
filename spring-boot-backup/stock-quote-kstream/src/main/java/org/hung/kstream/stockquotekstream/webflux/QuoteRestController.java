package org.hung.kstream.stockquotekstream.webflux;

import java.util.Comparator;

import org.hung.kstream.stockquotekstream.domain.Quote;
import org.hung.kstream.stockquotekstream.kafka.QuoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    
    private final Comparator<Quote> volumeComparator = (q1,q2) -> {
        if (q1.getVolume()==null && q2.getVolume()==null) {
            return 0;
        } else if (q1.getVolume()==null) {
            return -1;
        } else if (q2.getVolume()==null) {
            return 1;
        } else {
            return q1.getVolume().compareTo(q2.getVolume());
        }
    };

    private final Comparator<Quote> changePctComparator = 
        (q1,q2) -> Float.compare(q1.getChangePct(),q2.getChangePct());

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
        return quoteService.getAllQuote()
            .collectList()
            .map(list -> {
                list.sort(volumeComparator);
                return list.subList(Math.max(0,list.size()-topN),list.size()).reversed();
            })
            .flatMapIterable(list -> list);
    }

    @GetMapping("/gainers")
    public Flux<Quote> gainers(@RequestParam(name = "n", defaultValue = "5") int topN) {
        return quoteService.getAllQuote()
            .collectList()
            .map(list -> {
                list.sort(changePctComparator);
                return list.subList(Math.max(0,list.size()-topN),list.size()).reversed();
            })
            .flatMapIterable(list -> list);
    }

    @GetMapping("/losers")
    public Flux<Quote> losers(@RequestParam(name = "n", defaultValue = "5") int topN) {
        return quoteService.getAllQuote()
            .collectList()
            .map(list -> {
                list.sort(changePctComparator);
                return list.subList(0,Math.min(topN,list.size()));
            })
            .flatMapIterable(list -> list);
    }
}
