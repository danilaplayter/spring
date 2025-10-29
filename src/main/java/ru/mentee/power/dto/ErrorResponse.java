/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private OffsetDateTime timestamp;
    private String path;
    private String details;
}
