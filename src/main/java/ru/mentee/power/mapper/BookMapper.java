/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.mentee.power.api.generated.dto.BookResponse;
import ru.mentee.power.api.generated.dto.CreateBookRequest;
import ru.mentee.power.api.generated.dto.UpdateBookRequest;
import ru.mentee.power.domain.model.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {
    Book toBook(CreateBookRequest request);

    ru.mentee.power.api.generated.dto.Book toDto(Book book);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookFromRequest(UpdateBookRequest request, @MappingTarget Book book);

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    // Custom wrapper mapping to include success flag and data
    default BookResponse toBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setSuccess(true);
        response.setData(toDto(book));
        return response;
    }
}
