/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.mentee.power.api.generated.dto.RecommendationsDto;
import ru.mentee.power.api.generated.dto.RecommendedProductDto;

@Slf4j
@Service
public class RecommendationService {
    @Async("taskExecutor")
    public CompletableFuture<RecommendationsDto> getRecommendations(String category) {
        return CompletableFuture.supplyAsync(
                () -> {
                    List<RecommendedProductDto> recommendations;
                    if ("Electronics".equals(category)) {
                        RecommendedProductDto rec1 = new RecommendedProductDto();
                        rec1.setId("101");
                        rec1.setName("Игровая мышь Razer DeathAdder");
                        rec1.setPrice(2999.99f);
                        rec1.setImageUrl(URI.create("https://example.com/images/mouse.jpg"));
                        rec1.setSimilarityScore(0.95f);

                        RecommendedProductDto rec2 = new RecommendedProductDto();
                        rec2.setId("102");
                        rec2.setName("Механическая клавиатура HyperX");
                        rec2.setPrice(5999.99f);
                        rec2.setImageUrl(URI.create("https://example.com/images/keyboard.jpg"));
                        rec2.setSimilarityScore(0.92f);

                        RecommendedProductDto rec3 = new RecommendedProductDto();
                        rec3.setId("103");
                        rec3.setName("Игровой монитор ASUS 27\"");
                        rec3.setPrice(34999.99f);
                        rec3.setImageUrl(URI.create("https://example.com/images/monitor.jpg"));
                        rec3.setSimilarityScore(0.88f);

                        recommendations = Arrays.asList(rec1, rec2, rec3);
                    } else {
                        recommendations = Collections.emptyList();
                    }

                    RecommendationsDto recommendationsDto = new RecommendationsDto();
                    recommendationsDto.setSimilarProducts(recommendations);

                    return recommendationsDto;
                });
    }
}
