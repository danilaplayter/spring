/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mentee.power.domain.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    boolean existsByIsbn(String isbn);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Book> findByIsbn(String isbn, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCaseAndIsbn(
            String author, String isbn, Pageable pageable);
}
