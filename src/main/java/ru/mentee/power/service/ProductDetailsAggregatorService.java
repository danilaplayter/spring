/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mentee.power.api.generated.dto.InventoryDto;
import ru.mentee.power.api.generated.dto.ProductDetailsDto;
import ru.mentee.power.api.generated.dto.ProductInfoDto;
import ru.mentee.power.api.generated.dto.RecommendationsDto;
import ru.mentee.power.api.generated.dto.ReviewsDto;
import ru.mentee.power.api.generated.dto.ServiceError;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDetailsAggregatorService {
    private final ProductInfoService productInfoService;
    private final ReviewService reviewService;
    private final InventoryService inventoryService;
    private final RecommendationService recommendationService;

    @Value("${product.details.timeout.product-info:1000}")
    private long productInfoTimeout;

    @Value("${product.details.timeout.reviews:500}")
    private long reviewsTimeout;

    @Value("${product.details.timeout.inventory:500}")
    private long inventoryTimeout;

    @Value("${product.details.timeout.recommendations:800}")
    private long recommendationsTimeout;

    public CompletableFuture<ProductDetailsDto> getProductDetails(String productId) {

        List<ServiceError> errors = new ArrayList<>();

        CompletableFuture<ProductInfoDto> productInfoFuture =
                productInfoService
                        .getProductInfo(productId)
                        .orTimeout(productInfoTimeout, TimeUnit.MILLISECONDS)
                        .exceptionally(
                                ex -> {
                                    log.error(
                                            "Ошибка получения информации о продукте: {}",
                                            ex.getMessage());

                                    ProductInfoDto fallback = new ProductInfoDto();
                                    fallback.setId(productId);
                                    fallback.setName("Неизвестный товар");
                                    fallback.setCategory("Unknown");
                                    fallback.setPrice(0.0f);
                                    return fallback;
                                });

        return productInfoFuture.thenCompose(
                productInfo -> {
                    CompletableFuture<ReviewsDto> reviewsFuture =
                            reviewService
                                    .getReviews(productId)
                                    .orTimeout(reviewsTimeout, TimeUnit.MILLISECONDS)
                                    .exceptionally(
                                            ex -> {
                                                log.warn(
                                                        "Ошибка получения отзывов: {}",
                                                        ex.getMessage());

                                                // Пустой fallback
                                                ReviewsDto fallback = new ReviewsDto();
                                                fallback.setAverageRating(0.0f);
                                                fallback.setTotalReviews(0);
                                                fallback.setReviews(new ArrayList<>());
                                                return fallback;
                                            });

                    CompletableFuture<InventoryDto> inventoryFuture =
                            inventoryService
                                    .getInventory(productId)
                                    .orTimeout(inventoryTimeout, TimeUnit.MILLISECONDS)
                                    .exceptionally(
                                            ex -> {
                                                log.warn(
                                                        "Ошибка получения остатков: {}",
                                                        ex.getMessage());

                                                // Fallback
                                                InventoryDto fallback = new InventoryDto();
                                                fallback.setAvailable(false);
                                                fallback.setQuantity(0);
                                                fallback.setWarehouses(new ArrayList<>());
                                                return fallback;
                                            });

                    CompletableFuture<RecommendationsDto> recommendationsFuture =
                            recommendationService
                                    .getRecommendations(productInfo.getCategory())
                                    .orTimeout(recommendationsTimeout, TimeUnit.MILLISECONDS)
                                    .exceptionally(
                                            ex -> {
                                                log.warn(
                                                        "Ошибка получения рекомендаций: {}",
                                                        ex.getMessage());

                                                // Пустой fallback
                                                RecommendationsDto fallback =
                                                        new RecommendationsDto();
                                                fallback.setSimilarProducts(new ArrayList<>());
                                                return fallback;
                                            });

                    return CompletableFuture.allOf(
                                    reviewsFuture, inventoryFuture, recommendationsFuture)
                            .thenApply(
                                    v -> {
                                        ProductDetailsDto result = new ProductDetailsDto();
                                        result.setProductInfo(productInfo);
                                        result.setReviews(reviewsFuture.join());
                                        result.setInventory(inventoryFuture.join());
                                        result.setRecommendations(recommendationsFuture.join());
                                        result.setErrors(errors);

                                        log.info(
                                                "Успешно агрегирована информация о продукте: {}",
                                                productId);
                                        return result;
                                    });
                });
    }
}
