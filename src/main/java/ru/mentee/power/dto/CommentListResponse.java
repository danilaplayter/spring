/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponse {
    private List<Comment> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
