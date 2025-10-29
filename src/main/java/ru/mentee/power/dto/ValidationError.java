/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
    private String rejectedValue;
}
