package com.threeboys.toneup.search.service;

import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.Suggestion;
import com.redislabs.lettusearch.SuggetOptions;
import com.threeboys.toneup.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSearchService implements CommandLineRunner {
    private final RedisTemplate<String, String> redisTemplate;
    private final StatefulRediSearchConnection<String, String> searchConnection;

    private final ProductRepository productRepository;

    private final String autoCompleteKey = "product-autocomplete";

    public List<Suggestion<String>> autoComplete(String keyword) {
        RediSearchCommands<String, String> commands = searchConnection.sync();
        SuggetOptions options = SuggetOptions.builder().max(20L).withScores(true).build();
        return commands.sugget(autoCompleteKey, keyword, options);

    }

    @Override
    public void run(String... args) {
        try {
            RediSearchCommands<String, String> commands = searchConnection.sync();
            productRepository.findAll().forEach(product -> {
                if(product.getBrand() != null){
                    commands.sugadd(autoCompleteKey,
                            Suggestion.builder(product.getBrand()).score(5.0).build(), true);
                }
                if(product.getProductName() != null){
                    commands.sugadd(autoCompleteKey,
                            Suggestion.builder(product.getProductName()).score(1.0).build(), true);
                }
            });
            log.info("[RedisSearch] 자동완성 초기화 완료");
        } catch (Exception e) {
            log.error("[RedisSearch] 자동완성 초기화 실패", e);
        }
    }
}
