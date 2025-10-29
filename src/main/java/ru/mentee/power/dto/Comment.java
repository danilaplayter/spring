/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private UUID id;
    private String text;
    private String author;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
