/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Текст комментария не может превышать 2000 символов")
    private String text;

    @NotBlank(message = "Автор комментария не может быть пустым")
    @Size(max = 255, message = "Имя автора не может превышать 255 символов")
    private String author;
}
