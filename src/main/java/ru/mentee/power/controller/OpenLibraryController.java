/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.domain.dto.OpenLibrarySearchResponse;
import ru.mentee.power.service.OpenLibraryService;

@RestController
@RequestMapping("/api/v3/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Open Library", description = "API для поиска книг через Open Library")
public class OpenLibraryController {

    private final OpenLibraryService openLibraryService;

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<OpenLibrarySearchResponse> searchByISBN(
            @Parameter(
                            description = "ISBN книги (10 или 13 цифр, может содержать дефисы)",
                            required = true,
                            example = "9780140328721")
                    @PathVariable
                    String isbn) {
        log.info("REST: Searching book by ISBN: {}", isbn);

        OpenLibrarySearchResponse response = openLibraryService.findBookByISBN(isbn);
        return ResponseEntity.ok(response);
    }
}
