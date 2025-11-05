/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mentee.power.domain.dto.OpenLibrarySearchResponse;

@FeignClient(
        name = "pen-api-library-client",
        url = "${openlibrary.api.url}",
        configuration = ru.mentee.power.config.FeignConfig.class)
public interface OpenLibraryClient {

    @GetMapping("/search.json")
    OpenLibrarySearchResponse searchByISBN(@RequestParam("isbn") String isbn);
}
