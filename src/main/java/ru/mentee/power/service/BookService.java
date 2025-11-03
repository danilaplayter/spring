/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.api.generated.dto.BookListResponse;
import ru.mentee.power.api.generated.dto.BookListResponseData;
import ru.mentee.power.api.generated.dto.BookResponse;
import ru.mentee.power.api.generated.dto.CreateBookRequest;
import ru.mentee.power.api.generated.dto.UpdateBookRequest;
import ru.mentee.power.domain.model.Book;
import ru.mentee.power.domain.repository.BookRepository;
import ru.mentee.power.mapper.BookMapper;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public BookResponse createBook(CreateBookRequest request) {
        log.info("Создание книги пользователем с правами LIBRARIAN/ADMIN");
        if (repository.existsByIsbn(request.getIsbn())) {
            throw new IllegalStateException("BOOK_ALREADY_EXISTS");
        }
        Book saved = repository.save(mapper.toBook(request));
        return mapper.toBookResponse(saved);
    }

    @Transactional(readOnly = true)
    // Чтение книги доступно всем, поэтому не нужна аннотация
    public BookResponse getBookById(UUID id) {
        Book book =
                repository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("BOOK_NOT_FOUND"));
        return mapper.toBookResponse(book);
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public void deleteBookById(UUID id) {
        log.info("Удаление книги пользователем с правами LIBRARIAN/ADMIN");
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("BOOK_NOT_FOUND");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    // Получение списка книг доступно всем
    public BookListResponse getBooks(String author, String isbn, Pageable pageable) {
        Page<Book> booksPage;

        if (author != null && isbn != null) {
            booksPage = repository.findByAuthorContainingIgnoreCaseAndIsbn(author, isbn, pageable);
        } else if (author != null) {
            booksPage = repository.findByAuthorContainingIgnoreCase(author, pageable);
        } else if (isbn != null) {
            booksPage = repository.findByIsbn(isbn, pageable);
        } else {
            booksPage = repository.findAll(pageable);
        }

        BookListResponseData data = new BookListResponseData();
        data.setContent(booksPage.getContent().stream().map(mapper::toDto).toList());
        data.setTotalElements((int) booksPage.getTotalElements());
        data.setTotalPages(booksPage.getTotalPages());
        data.setSize(booksPage.getSize());
        data.setNumber(booksPage.getNumber());
        data.setFirst(booksPage.isFirst());
        data.setLast(booksPage.isLast());

        BookListResponse response = new BookListResponse();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public BookResponse updateBook(UUID id, UpdateBookRequest request) {
        log.info("Обновление книги пользователем с правами LIBRARIAN/ADMIN");
        Book book =
                repository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("BOOK_NOT_FOUND"));
        if (request.getIsbn() != null
                && !request.getIsbn().equals(book.getIsbn())
                && repository.existsByIsbn(request.getIsbn())) {
            throw new IllegalStateException("BOOK_ALREADY_EXISTS");
        }
        mapper.updateBookFromRequest(request, book);
        Book saved = repository.save(book);
        return mapper.toBookResponse(saved);
    }
}
