/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.mentee.power.api.generated.dto.ProductInfoDto;

@Service
public class ProductInfoService {

    @Async("taskExecutor")
    public CompletableFuture<ProductInfoDto> getProductInfo(String productId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    Map<String, Object> specs = new HashMap<>();
                    specs.put("processor", "Intel Core i7-12700H");
                    specs.put("ram", "16GB DDR5");
                    specs.put("storage", "512GB NVMe SSD");
                    specs.put("gpu", "NVIDIA RTX 4070");
                    specs.put("screen", "15.6\" FHD 144Hz");

                    ProductInfoDto productInfo = new ProductInfoDto();
                    productInfo.setId(productId);
                    productInfo.setName("Ноутбук Gaming Pro");
                    productInfo.setDescription("Мощный игровой ноутбук для профессионалов");
                    productInfo.setPrice(89999.99f);
                    productInfo.setCategory("Electronics");
                    productInfo.setBrand("TechBrand");
                    productInfo.setImageUrl(URI.create("https://example.com/images/laptop.jpg"));
                    productInfo.setSpecifications(specs);

                    return productInfo;
                });
    }
}
