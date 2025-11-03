/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.api.generated.controller.DefaultApi;
import ru.mentee.power.api.generated.dto.BookListResponse;
import ru.mentee.power.api.generated.dto.BookResponse;
import ru.mentee.power.api.generated.dto.CreateBookRequest;
import ru.mentee.power.api.generated.dto.UpdateBookRequest;
import ru.mentee.power.service.BookService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BookController implements DefaultApi {

    private final BookService service;

    @Override
    public ResponseEntity<BookListResponse> apiBooksGet(
            String author, String isbn, Integer page, Integer size) {
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        Pageable pageable = PageRequest.of(p, s);
        BookListResponse response = service.getBooks(author, isbn, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> apiBooksIdDelete(UUID id) {
        log.info("Удаляем книгу с id: {}", id);

        service.deleteBookById(id);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BookResponse> apiBooksIdGet(UUID id) {
        log.info("Берём книгу с id: {}", id);

        BookResponse response = service.getBookById(id);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BookResponse> apiBooksIdPut(
            UUID id, @Valid UpdateBookRequest updateBookRequest) {
        log.info("Обновляем книгу {} данными {}", id, updateBookRequest);
        BookResponse response = service.updateBook(id, updateBookRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BookResponse> apiBooksPost(@Valid CreateBookRequest createBookRequest) {
        log.info("Создание новой книги: {}", createBookRequest);
        BookResponse response = service.createBook(createBookRequest);

        return ResponseEntity.created(URI.create("/api/books/" + response.getData().getId()))
                .body(response);
    }
}
