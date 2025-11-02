/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;
import ru.mentee.power.api.generated.dto.ReviewDto;
import ru.mentee.power.api.generated.dto.ReviewsDto;

@Service
public class ReviewService {

    public CompletableFuture<ReviewsDto> getReviews(String productId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    ReviewDto review1 = new ReviewDto();
                    review1.setId("123");
                    review1.setAuthor("Иван Петров");
                    review1.setRating(5);
                    review1.setComment("Отличный товар! Быстрая доставка, качество на высоте.");
                    review1.setDate(OffsetDateTime.now().minusDays(5));

                    ReviewDto review2 = new ReviewDto();
                    review2.setId("r2");
                    review2.setAuthor("Мария Сидорова");
                    review2.setRating(4);
                    review2.setComment("Хорошо, но дороговато. В целом доволен покупкой.");
                    review2.setDate(OffsetDateTime.now().minusDays(10));

                    ReviewDto review3 = new ReviewDto();
                    review3.setId("r3");
                    review3.setAuthor("Петр Иванов");
                    review3.setRating(5);
                    review3.setComment("Рекомендую! Играю в Cyberpunk на максималках - летает!");
                    review3.setDate(OffsetDateTime.now().minusDays(15));

                    List<ReviewDto> reviews = Arrays.asList(review1, review2, review3);

                    // Вычисляем средний рейтинг
                    float avgRating =
                            (float)
                                    reviews.stream()
                                            .mapToInt(ReviewDto::getRating)
                                            .average()
                                            .orElse(0.0);

                    // Создаем агрегированный DTO
                    ReviewsDto reviewsDto = new ReviewsDto();
                    reviewsDto.setAverageRating(avgRating);
                    reviewsDto.setTotalReviews(reviews.size());
                    reviewsDto.setReviews(reviews);

                    return reviewsDto;
                });
    }
}
