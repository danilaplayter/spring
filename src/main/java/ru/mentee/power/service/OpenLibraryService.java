/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mentee.power.client.OpenLibraryClient;
import ru.mentee.power.domain.dto.OpenLibrarySearchResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenLibraryService {

    private final OpenLibraryClient openLibraryClient;
    private static final String OPEN_LIBRARY_SERVICE = "open-library-client";

    @CircuitBreaker(name = OPEN_LIBRARY_SERVICE, fallbackMethod = "findBookByISBNFallback")
    @Retry(name = OPEN_LIBRARY_SERVICE, fallbackMethod = "findBookByISBNRetryFallback")
    public OpenLibrarySearchResponse findBookByISBN(String isbn) {
        log.info("Searching book by ISBN: {}", isbn);

        OpenLibrarySearchResponse response = openLibraryClient.searchByISBN(isbn);
        if (response.getDocs() != null && !response.getDocs().isEmpty()) {
            log.info("Found {} book(s) for ISBN: {}", response.getDocs().size(), isbn);

            OpenLibrarySearchResponse.BookDoc firstBook = response.getDocs().get(0);
            log.debug(
                    "First book: title='{}', authors={}, year={}",
                    firstBook.getTitle(),
                    firstBook.getAuthorName(),
                    firstBook.getFirstPublishYear());
        } else {
            log.info("No books found for ISBN: {}", isbn);
        }

        return response;
    }

    public OpenLibrarySearchResponse findBookByISBNFallback(String isbn, Exception e) {
        log.error("Circuit Breaker fallback for ISBN: {}, error: {}", isbn, e.getMessage());

        OpenLibrarySearchResponse fallbackResponse = new OpenLibrarySearchResponse();
        fallbackResponse.setDocs(java.util.Collections.emptyList());
        fallbackResponse.setNumFound(0);
        fallbackResponse.setStart(0);

        return fallbackResponse;
    }

    public OpenLibrarySearchResponse findBookByISBNRetryFallback(String isbn, Exception e) {
        log.error("All retry attempts failed for ISBN: {}, error: {}", isbn, e.getMessage());

        OpenLibrarySearchResponse fallbackResponse = new OpenLibrarySearchResponse();
        fallbackResponse.setDocs(java.util.Collections.emptyList());
        fallbackResponse.setNumFound(0);
        fallbackResponse.setStart(0);

        return fallbackResponse;
    }

    public String getCoverUrl(Long coverId, String size) {
        if (coverId == null) {
            return null;
        }
        String sizeParam = (size != null && !size.isEmpty()) ? size.toUpperCase() : "M";
        return String.format("https://covers.openlibrary.org/b/id/%d-%s.jpg", coverId, sizeParam);
    }

    public String getCoverUrlByISBN(String isbn, String size) {
        if (isbn == null || isbn.isEmpty()) {
            return null;
        }
        String sizeParam = (size != null && !size.isEmpty()) ? size.toUpperCase() : "M";
        return String.format("https://covers.openlibrary.org/b/isbn/%s-%s.jpg", isbn, sizeParam);
    }

    public String getBookUrl(String workKey) {
        if (workKey == null || workKey.isEmpty()) {
            return null;
        }
        String cleanKey = workKey.startsWith("/works/") ? workKey : "/works/" + workKey;
        return "https://openlibrary.org" + cleanKey;
    }

    public String getAuthorUrl(String authorKey) {
        if (authorKey == null || authorKey.isEmpty()) {
            return null;
        }
        String cleanKey = authorKey.startsWith("/authors/") ? authorKey : "/authors/" + authorKey;
        return "https://openlibrary.org" + cleanKey;
    }
}
