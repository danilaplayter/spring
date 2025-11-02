/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.mentee.power.api.generated.dto.InventoryDto;
import ru.mentee.power.api.generated.dto.ProductDetailsDto;
import ru.mentee.power.api.generated.dto.ProductInfoDto;
import ru.mentee.power.api.generated.dto.RecommendationsDto;
import ru.mentee.power.api.generated.dto.RecommendedProductDto;
import ru.mentee.power.api.generated.dto.ReviewDto;
import ru.mentee.power.api.generated.dto.ReviewsDto;
import ru.mentee.power.api.generated.dto.WarehouseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ProductDetailsAggregatorService")
class ProductDetailsAggregatorServiceTest {

    @Mock private ProductInfoService productInfoService;

    @Mock private ReviewService reviewService;

    @Mock private InventoryService inventoryService;

    @Mock private RecommendationService recommendationService;

    @InjectMocks private ProductDetailsAggregatorService aggregatorService;

    private static final String PRODUCT_ID = "test-product-123";
    private static final String CATEGORY_ELECTRONICS = "Electronics";
    private static final long PRODUCT_INFO_TIMEOUT = 1000L;
    private static final long REVIEWS_TIMEOUT = 500L;
    private static final long INVENTORY_TIMEOUT = 500L;
    private static final long RECOMMENDATIONS_TIMEOUT = 800L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aggregatorService, "productInfoTimeout", PRODUCT_INFO_TIMEOUT);
        ReflectionTestUtils.setField(aggregatorService, "reviewsTimeout", REVIEWS_TIMEOUT);
        ReflectionTestUtils.setField(aggregatorService, "inventoryTimeout", INVENTORY_TIMEOUT);
        ReflectionTestUtils.setField(
                aggregatorService, "recommendationsTimeout", RECOMMENDATIONS_TIMEOUT);
    }

    @Test
    @DisplayName("Успешная агрегация данных из всех сервисов")
    void testSuccessfulAggregation() throws ExecutionException, InterruptedException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        ReviewsDto reviews = createReviewsDto();
        InventoryDto inventory = createInventoryDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(reviews));
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(inventory));
        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertEquals(PRODUCT_ID, result.getProductInfo().getId());
        assertEquals(CATEGORY_ELECTRONICS, result.getProductInfo().getCategory());

        assertNotNull(result.getReviews());
        assertEquals(3, result.getReviews().getTotalReviews());
        assertEquals(4.67f, result.getReviews().getAverageRating(), 0.01f);

        assertNotNull(result.getInventory());
        assertTrue(result.getInventory().getAvailable());
        assertEquals(26, result.getInventory().getQuantity());

        assertNotNull(result.getRecommendations());
        assertEquals(3, result.getRecommendations().getSimilarProducts().size());

        // Verify that services were called
        verify(productInfoService, times(1)).getProductInfo(PRODUCT_ID);
        verify(reviewService, times(1)).getReviews(PRODUCT_ID);
        verify(inventoryService, times(1)).getInventory(PRODUCT_ID);
        verify(recommendationService, times(1)).getRecommendations(CATEGORY_ELECTRONICS);
    }

    @Test
    @DisplayName("Таймаут ProductInfoService - должен вернуть fallback")
    void testProductInfoServiceTimeout()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        CompletableFuture<ProductInfoDto> delayedFuture = new CompletableFuture<>();
        when(productInfoService.getProductInfo(PRODUCT_ID)).thenReturn(delayedFuture);

        // Mock other services for the fallback case (when category is "Unknown")
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Timeout")));
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Timeout")));
        when(recommendationService.getRecommendations("Unknown"))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Timeout")));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);

        // Wait for timeout
        Thread.sleep(PRODUCT_INFO_TIMEOUT + 200);

        ProductDetailsDto result = future.get(2, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertEquals(PRODUCT_ID, result.getProductInfo().getId());
        assertEquals("Неизвестный товар", result.getProductInfo().getName());
        assertEquals("Unknown", result.getProductInfo().getCategory());
        assertEquals(0.0f, result.getProductInfo().getPrice());

        // Verify fallback data for other services
        assertNotNull(result.getReviews());
        assertEquals(0, result.getReviews().getTotalReviews());
        assertEquals(0.0f, result.getReviews().getAverageRating());

        assertNotNull(result.getInventory());
        assertFalse(result.getInventory().getAvailable());
        assertEquals(0, result.getInventory().getQuantity());

        assertNotNull(result.getRecommendations());
        assertEquals(0, result.getRecommendations().getSimilarProducts().size());
    }

    @Test
    @DisplayName("Таймаут ReviewService - должен вернуть fallback")
    void testReviewServiceTimeout()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        InventoryDto inventory = createInventoryDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));

        CompletableFuture<ReviewsDto> delayedFuture = new CompletableFuture<>();
        when(reviewService.getReviews(PRODUCT_ID)).thenReturn(delayedFuture);

        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(inventory));
        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);

        // Wait for timeout
        Thread.sleep(REVIEWS_TIMEOUT + 200);

        ProductDetailsDto result = future.get(2, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertNotNull(result.getReviews());
        assertEquals(0, result.getReviews().getTotalReviews());
        assertEquals(0.0f, result.getReviews().getAverageRating());
        assertTrue(result.getReviews().getReviews().isEmpty());

        assertNotNull(result.getInventory());
        assertTrue(result.getInventory().getAvailable());

        assertNotNull(result.getRecommendations());
        assertEquals(3, result.getRecommendations().getSimilarProducts().size());
    }

    @Test
    @DisplayName("Таймаут InventoryService - должен вернуть fallback")
    void testInventoryServiceTimeout()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        ReviewsDto reviews = createReviewsDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(reviews));

        CompletableFuture<InventoryDto> delayedFuture = new CompletableFuture<>();
        when(inventoryService.getInventory(PRODUCT_ID)).thenReturn(delayedFuture);

        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);

        // Wait for timeout
        Thread.sleep(INVENTORY_TIMEOUT + 200);

        ProductDetailsDto result = future.get(2, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertNotNull(result.getReviews());
        assertEquals(3, result.getReviews().getTotalReviews());

        assertNotNull(result.getInventory());
        assertFalse(result.getInventory().getAvailable());
        assertEquals(0, result.getInventory().getQuantity());
        assertTrue(result.getInventory().getWarehouses().isEmpty());

        assertNotNull(result.getRecommendations());
        assertEquals(3, result.getRecommendations().getSimilarProducts().size());
    }

    @Test
    @DisplayName("Таймаут RecommendationService - должен вернуть fallback")
    void testRecommendationServiceTimeout()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        ReviewsDto reviews = createReviewsDto();
        InventoryDto inventory = createInventoryDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(reviews));
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(inventory));

        CompletableFuture<RecommendationsDto> delayedFuture = new CompletableFuture<>();
        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(delayedFuture);

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);

        // Wait for timeout
        Thread.sleep(RECOMMENDATIONS_TIMEOUT + 200);

        ProductDetailsDto result = future.get(2, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertNotNull(result.getReviews());
        assertNotNull(result.getInventory());
        assertNotNull(result.getRecommendations());
        assertEquals(0, result.getRecommendations().getSimilarProducts().size());
    }

    @Test
    @DisplayName("Ошибка ProductInfoService - должен вернуть fallback")
    void testProductInfoServiceError() throws ExecutionException, InterruptedException {
        // Arrange
        RuntimeException exception = new RuntimeException("ProductInfo service error");
        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // Mock other services for the fallback case (when category is "Unknown")
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error")));
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error")));
        when(recommendationService.getRecommendations("Unknown"))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Error")));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertEquals(PRODUCT_ID, result.getProductInfo().getId());
        assertEquals("Неизвестный товар", result.getProductInfo().getName());
        assertEquals("Unknown", result.getProductInfo().getCategory());
    }

    @Test
    @DisplayName("Ошибка ReviewService - должен вернуть fallback")
    void testReviewServiceError() throws ExecutionException, InterruptedException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        InventoryDto inventory = createInventoryDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));

        RuntimeException exception = new RuntimeException("Review service error");
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(exception));

        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(inventory));
        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertNotNull(result.getReviews());
        assertEquals(0, result.getReviews().getTotalReviews());
        assertEquals(0.0f, result.getReviews().getAverageRating());
    }

    @Test
    @DisplayName("Ошибка InventoryService - должен вернуть fallback")
    void testInventoryServiceError() throws ExecutionException, InterruptedException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        ReviewsDto reviews = createReviewsDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(reviews));

        RuntimeException exception = new RuntimeException("Inventory service error");
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.failedFuture(exception));

        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertNotNull(result.getReviews());
        assertNotNull(result.getInventory());
        assertFalse(result.getInventory().getAvailable());
        assertEquals(0, result.getInventory().getQuantity());
    }

    @Test
    @DisplayName("Ошибка RecommendationService - должен вернуть fallback")
    void testRecommendationServiceError() throws ExecutionException, InterruptedException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        ReviewsDto reviews = createReviewsDto();
        InventoryDto inventory = createInventoryDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(reviews));
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(inventory));

        RuntimeException exception = new RuntimeException("Recommendation service error");
        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertNotNull(result.getReviews());
        assertNotNull(result.getInventory());
        assertNotNull(result.getRecommendations());
        assertEquals(0, result.getRecommendations().getSimilarProducts().size());
    }

    @Test
    @DisplayName(
            "ReviewService и InventoryService должны вызываться параллельно после получения"
                    + " ProductInfo")
    void testParallelExecutionOfReviewAndInventoryServices()
            throws ExecutionException, InterruptedException {
        // Arrange
        ProductInfoDto productInfo = createProductInfoDto();
        ReviewsDto reviews = createReviewsDto();
        InventoryDto inventory = createInventoryDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        long[] reviewCallTime = new long[1];
        long[] inventoryCallTime = new long[1];

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));

        when(reviewService.getReviews(PRODUCT_ID))
                .thenAnswer(
                        invocation -> {
                            reviewCallTime[0] = System.currentTimeMillis();
                            return CompletableFuture.completedFuture(reviews);
                        });

        when(inventoryService.getInventory(PRODUCT_ID))
                .thenAnswer(
                        invocation -> {
                            inventoryCallTime[0] = System.currentTimeMillis();
                            return CompletableFuture.completedFuture(inventory);
                        });

        when(recommendationService.getRecommendations(CATEGORY_ELECTRONICS))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        long startTime = System.currentTimeMillis();
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result);
        // Проверяем, что оба сервиса были вызваны
        verify(reviewService, times(1)).getReviews(PRODUCT_ID);
        verify(inventoryService, times(1)).getInventory(PRODUCT_ID);

        // Проверяем, что они были вызваны практически одновременно
        // (разница во времени должна быть небольшой, менее 100мс)
        long timeDifference = Math.abs(reviewCallTime[0] - inventoryCallTime[0]);
        assertTrue(
                timeDifference < 100,
                "ReviewService и InventoryService должны вызываться параллельно");
    }

    @Test
    @DisplayName("RecommendationService должен вызываться с категорией из ProductInfoService")
    void testRecommendationServiceDependsOnCategory()
            throws ExecutionException, InterruptedException {
        // Arrange
        String category = "Electronics";
        ProductInfoDto productInfo = createProductInfoDto();
        productInfo.setCategory(category);

        ReviewsDto reviews = createReviewsDto();
        InventoryDto inventory = createInventoryDto();
        RecommendationsDto recommendations = createRecommendationsDto();

        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo));
        when(reviewService.getReviews(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(reviews));
        when(inventoryService.getInventory(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(inventory));
        when(recommendationService.getRecommendations(category))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);
        ProductDetailsDto result = future.get();

        // Assert
        assertNotNull(result);
        verify(recommendationService, times(1)).getRecommendations(category);

        // Проверяем с другой категорией
        reset(recommendationService);
        String otherCategory = "Books";
        ProductInfoDto productInfo2 = createProductInfoDto();
        productInfo2.setCategory(otherCategory);
        when(productInfoService.getProductInfo(PRODUCT_ID))
                .thenReturn(CompletableFuture.completedFuture(productInfo2));
        when(recommendationService.getRecommendations(otherCategory))
                .thenReturn(CompletableFuture.completedFuture(recommendations));

        future = aggregatorService.getProductDetails(PRODUCT_ID);
        result = future.get();

        verify(recommendationService, times(1)).getRecommendations(otherCategory);
    }

    @Test
    @DisplayName("Все сервисы с таймаутами - должны вернуть fallback данные")
    void testAllServicesTimeout()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Arrange
        CompletableFuture<ProductInfoDto> delayedProductInfo = new CompletableFuture<>();
        CompletableFuture<ReviewsDto> delayedReviews = new CompletableFuture<>();
        CompletableFuture<InventoryDto> delayedInventory = new CompletableFuture<>();
        CompletableFuture<RecommendationsDto> delayedRecommendations = new CompletableFuture<>();

        when(productInfoService.getProductInfo(PRODUCT_ID)).thenReturn(delayedProductInfo);
        when(reviewService.getReviews(PRODUCT_ID)).thenReturn(delayedReviews);
        when(inventoryService.getInventory(PRODUCT_ID)).thenReturn(delayedInventory);
        when(recommendationService.getRecommendations(anyString()))
                .thenReturn(delayedRecommendations);

        // Act
        CompletableFuture<ProductDetailsDto> future =
                aggregatorService.getProductDetails(PRODUCT_ID);

        // Wait for all timeouts
        Thread.sleep(Math.max(PRODUCT_INFO_TIMEOUT, RECOMMENDATIONS_TIMEOUT) + 300);

        ProductDetailsDto result = future.get(3, TimeUnit.SECONDS);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getProductInfo());
        assertEquals("Неизвестный товар", result.getProductInfo().getName());
        assertEquals("Unknown", result.getProductInfo().getCategory());

        assertNotNull(result.getReviews());
        assertEquals(0, result.getReviews().getTotalReviews());

        assertNotNull(result.getInventory());
        assertFalse(result.getInventory().getAvailable());
        assertEquals(0, result.getInventory().getQuantity());

        assertNotNull(result.getRecommendations());
        assertEquals(0, result.getRecommendations().getSimilarProducts().size());
    }

    // Helper methods

    private ProductInfoDto createProductInfoDto() {
        ProductInfoDto dto = new ProductInfoDto();
        dto.setId(PRODUCT_ID);
        dto.setName("Ноутбук Gaming Pro");
        dto.setDescription("Мощный игровой ноутбук");
        dto.setPrice(89999.99f);
        dto.setCategory(CATEGORY_ELECTRONICS);
        dto.setBrand("TechBrand");
        dto.setImageUrl(URI.create("https://example.com/images/laptop.jpg"));

        java.util.Map<String, Object> specs = new java.util.HashMap<>();
        specs.put("processor", "Intel Core i7-12700H");
        specs.put("ram", "16GB DDR5");
        specs.put("storage", "512GB NVMe SSD");
        dto.setSpecifications(specs);

        return dto;
    }

    private ReviewsDto createReviewsDto() {
        ReviewDto review1 = new ReviewDto();
        review1.setId("r1");
        review1.setAuthor("Иван Петров");
        review1.setRating(5);
        review1.setComment("Отличный товар!");
        review1.setDate(OffsetDateTime.now().minusDays(5));

        ReviewDto review2 = new ReviewDto();
        review2.setId("r2");
        review2.setAuthor("Мария Сидорова");
        review2.setRating(4);
        review2.setComment("Хорошо!");
        review2.setDate(OffsetDateTime.now().minusDays(10));

        ReviewDto review3 = new ReviewDto();
        review3.setId("r3");
        review3.setAuthor("Петр Иванов");
        review3.setRating(5);
        review3.setComment("Рекомендую!");
        review3.setDate(OffsetDateTime.now().minusDays(15));

        ReviewsDto dto = new ReviewsDto();
        dto.setReviews(Arrays.asList(review1, review2, review3));
        dto.setTotalReviews(3);
        dto.setAverageRating(4.67f);

        return dto;
    }

    private InventoryDto createInventoryDto() {
        WarehouseDto warehouse1 = new WarehouseDto();
        warehouse1.setId("wh1");
        warehouse1.setName("Склад Москва");
        warehouse1.setQuantity(15);
        warehouse1.setCity("Москва");

        WarehouseDto warehouse2 = new WarehouseDto();
        warehouse2.setId("wh2");
        warehouse2.setName("Склад Санкт-Петербург");
        warehouse2.setQuantity(8);
        warehouse2.setCity("Санкт-Петербург");

        WarehouseDto warehouse3 = new WarehouseDto();
        warehouse3.setId("wh3");
        warehouse3.setName("Склад Казань");
        warehouse3.setQuantity(3);
        warehouse3.setCity("Казань");

        InventoryDto dto = new InventoryDto();
        dto.setWarehouses(Arrays.asList(warehouse1, warehouse2, warehouse3));
        dto.setQuantity(26);
        dto.setAvailable(true);

        return dto;
    }

    private RecommendationsDto createRecommendationsDto() {
        RecommendedProductDto rec1 = new RecommendedProductDto();
        rec1.setId("101");
        rec1.setName("Игровая мышь");
        rec1.setPrice(2999.99f);
        rec1.setImageUrl(URI.create("https://example.com/images/mouse.jpg"));
        rec1.setSimilarityScore(0.95f);

        RecommendedProductDto rec2 = new RecommendedProductDto();
        rec2.setId("102");
        rec2.setName("Клавиатура");
        rec2.setPrice(5999.99f);
        rec2.setImageUrl(URI.create("https://example.com/images/keyboard.jpg"));
        rec2.setSimilarityScore(0.92f);

        RecommendedProductDto rec3 = new RecommendedProductDto();
        rec3.setId("103");
        rec3.setName("Монитор");
        rec3.setPrice(34999.99f);
        rec3.setImageUrl(URI.create("https://example.com/images/monitor.jpg"));
        rec3.setSimilarityScore(0.88f);

        RecommendationsDto dto = new RecommendationsDto();
        dto.setSimilarProducts(Arrays.asList(rec1, rec2, rec3));

        return dto;
    }
}
