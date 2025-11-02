/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.mentee.power.api.generated.dto.InventoryDto;
import ru.mentee.power.api.generated.dto.WarehouseDto;

@Service
public class InventoryService {

    @Async("taskExecutor")
    public CompletableFuture<InventoryDto> getInventory(String productId) {
        return CompletableFuture.supplyAsync(
                () -> {
                    WarehouseDto warehouse1 = new WarehouseDto();
                    warehouse1.setId("wh1");
                    warehouse1.setName("Склад Москва Центральный");
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

                    List<WarehouseDto> warehouses =
                            Arrays.asList(warehouse1, warehouse2, warehouse3);

                    int totalQuantity =
                            warehouses.stream().mapToInt(WarehouseDto::getQuantity).sum();

                    InventoryDto inventory = new InventoryDto();
                    inventory.setAvailable(totalQuantity > 0);
                    inventory.setQuantity(totalQuantity);
                    inventory.setWarehouses(warehouses);

                    return inventory;
                });
    }
}
