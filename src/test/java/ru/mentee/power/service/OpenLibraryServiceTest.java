/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mentee.power.client.OpenLibraryClient;
import ru.mentee.power.domain.dto.OpenLibrarySearchResponse;

@ExtendWith(MockitoExtension.class)
class OpenLibraryServiceTest {

    @Mock private OpenLibraryClient openLibraryClient;

    @InjectMocks private OpenLibraryService openLibraryService;

    private OpenLibrarySearchResponse.BookDoc bookDoc;
    private OpenLibrarySearchResponse searchResponse;

    @BeforeEach
    void setUp() {
        bookDoc = new OpenLibrarySearchResponse.BookDoc();
        bookDoc.setKey("/works/OL45804W");
        bookDoc.setTitle("Fantastic Mr. Fox");
        bookDoc.setAuthorName(List.of("Roald Dahl"));
        bookDoc.setAuthorKey(List.of("OL34184A"));
        bookDoc.setFirstPublishYear(1970);
        bookDoc.setIsbn(List.of("9780140328721", "0140328726"));
        bookDoc.setPublisher(List.of("Puffin"));
        bookDoc.setLanguage(List.of("eng"));
        bookDoc.setCoverId(6498519L);
        bookDoc.setEditionCount(58);

        searchResponse = new OpenLibrarySearchResponse();
        searchResponse.setStart(0);
        searchResponse.setNumFound(1);
        searchResponse.setNumFoundExact(true);
        searchResponse.setDocs(List.of(bookDoc));
    }

    @Test
    void shouldFindBookByISBN_whenBookExists() {
        // Given
        String isbn = "9780140328721";
        when(openLibraryClient.searchByISBN(eq(isbn))).thenReturn(searchResponse);

        // When
        OpenLibrarySearchResponse result = openLibraryService.findBookByISBN(isbn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumFound()).isEqualTo(1);
        assertThat(result.getDocs()).hasSize(1);
        assertThat(result.getDocs().get(0).getTitle()).isEqualTo("Fantastic Mr. Fox");

        verify(openLibraryClient, times(1)).searchByISBN(isbn);
    }

    @Test
    void shouldReturnEmptyResult_whenBookNotFound() {
        // Given
        String isbn = "9999999999999";
        OpenLibrarySearchResponse emptyResponse = new OpenLibrarySearchResponse();
        emptyResponse.setStart(0);
        emptyResponse.setNumFound(0);
        emptyResponse.setDocs(Collections.emptyList());

        when(openLibraryClient.searchByISBN(eq(isbn))).thenReturn(emptyResponse);

        // When
        OpenLibrarySearchResponse result = openLibraryService.findBookByISBN(isbn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumFound()).isEqualTo(0);
        assertThat(result.getDocs()).isEmpty();

        verify(openLibraryClient, times(1)).searchByISBN(isbn);
    }

    @Test
    void shouldThrowException_whenClientFails() {
        // Given
        String isbn = "1234567890";
        when(openLibraryClient.searchByISBN(eq(isbn)))
                .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        assertThatThrownBy(() -> openLibraryService.findBookByISBN(isbn))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to search book by ISBN");

        verify(openLibraryClient, times(1)).searchByISBN(isbn);
    }

    @Test
    void shouldGenerateCoverUrl_withCoverId() {
        // Given
        Long coverId = 6498519L;
        String size = "M";

        // When
        String coverUrl = openLibraryService.getCoverUrl(coverId, size);

        // Then
        assertThat(coverUrl).isEqualTo("https://covers.openlibrary.org/b/id/6498519-M.jpg");
    }

    @Test
    void shouldGenerateCoverUrl_withDefaultSize() {
        // Given
        Long coverId = 6498519L;

        // When
        String coverUrl = openLibraryService.getCoverUrl(coverId, null);

        // Then
        assertThat(coverUrl).isEqualTo("https://covers.openlibrary.org/b/id/6498519-M.jpg");
    }

    @Test
    void shouldGenerateCoverUrl_withLargeSize() {
        // Given
        Long coverId = 123456L;
        String size = "L";

        // When
        String coverUrl = openLibraryService.getCoverUrl(coverId, size);

        // Then
        assertThat(coverUrl).isEqualTo("https://covers.openlibrary.org/b/id/123456-L.jpg");
    }

    @Test
    void shouldReturnNull_whenCoverIdIsNull() {
        // When
        String coverUrl = openLibraryService.getCoverUrl(null, "M");

        // Then
        assertThat(coverUrl).isNull();
    }

    @Test
    void shouldGenerateCoverUrlByISBN() {
        // Given
        String isbn = "9780140328721";
        String size = "M";

        // When
        String coverUrl = openLibraryService.getCoverUrlByISBN(isbn, size);

        // Then
        assertThat(coverUrl).isEqualTo("https://covers.openlibrary.org/b/isbn/9780140328721-M.jpg");
    }

    @Test
    void shouldReturnNull_whenISBNIsNull() {
        // When
        String coverUrl = openLibraryService.getCoverUrlByISBN(null, "M");

        // Then
        assertThat(coverUrl).isNull();
    }

    @Test
    void shouldReturnNull_whenISBNIsEmpty() {
        // When
        String coverUrl = openLibraryService.getCoverUrlByISBN("", "M");

        // Then
        assertThat(coverUrl).isNull();
    }

    @Test
    void shouldGenerateBookUrl() {
        // Given
        String workKey = "OL45804W";

        // When
        String bookUrl = openLibraryService.getBookUrl(workKey);

        // Then
        assertThat(bookUrl).isEqualTo("https://openlibrary.org/works/OL45804W");
    }

    @Test
    void shouldGenerateBookUrl_whenKeyHasPrefix() {
        // Given
        String workKey = "/works/OL45804W";

        // When
        String bookUrl = openLibraryService.getBookUrl(workKey);

        // Then
        assertThat(bookUrl).isEqualTo("https://openlibrary.org/works/OL45804W");
    }

    @Test
    void shouldGenerateAuthorUrl() {
        // Given
        String authorKey = "OL34184A";

        // When
        String authorUrl = openLibraryService.getAuthorUrl(authorKey);

        // Then
        assertThat(authorUrl).isEqualTo("https://openlibrary.org/authors/OL34184A");
    }

    @Test
    void shouldGenerateAuthorUrl_whenKeyHasPrefix() {
        // Given
        String authorKey = "/authors/OL34184A";

        // When
        String authorUrl = openLibraryService.getAuthorUrl(authorKey);

        // Then
        assertThat(authorUrl).isEqualTo("https://openlibrary.org/authors/OL34184A");
    }

    @Test
    void shouldHandleMultipleAuthors() {
        // Given
        String isbn = "1234567890";
        OpenLibrarySearchResponse.BookDoc multiAuthorBook = new OpenLibrarySearchResponse.BookDoc();
        multiAuthorBook.setTitle("Multi Author Book");
        multiAuthorBook.setAuthorName(List.of("Author One", "Author Two", "Author Three"));
        multiAuthorBook.setAuthorKey(List.of("OL1A", "OL2A", "OL3A"));

        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        response.setNumFound(1);
        response.setDocs(List.of(multiAuthorBook));

        when(openLibraryClient.searchByISBN(eq(isbn))).thenReturn(response);

        // When
        OpenLibrarySearchResponse result = openLibraryService.findBookByISBN(isbn);

        // Then
        assertThat(result.getDocs().get(0).getAuthorName())
                .containsExactly("Author One", "Author Two", "Author Three");
        assertThat(result.getDocs().get(0).getAuthorKey()).containsExactly("OL1A", "OL2A", "OL3A");
    }
}
