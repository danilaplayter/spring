/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.mapper;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.mentee.power.domain.model.CommentEntity;
import ru.mentee.power.domain.model.TaskEntity;
import ru.mentee.power.dto.Comment;
import ru.mentee.power.dto.CommentListResponse;
import ru.mentee.power.dto.CreateCommentRequest;

@Component
public class CommentMapper {

    public CommentEntity toEntity(CreateCommentRequest createCommentRequest, UUID taskId) {
        CommentEntity entity = new CommentEntity();
        entity.setText(createCommentRequest.getText());
        entity.setAuthor(createCommentRequest.getAuthor());

        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        entity.setTask(task);

        return entity;
    }

    public Comment toDto(CommentEntity commentEntity) {
        Comment dto = new Comment();
        dto.setId(commentEntity.getId());
        dto.setText(commentEntity.getText());
        dto.setAuthor(commentEntity.getAuthor());
        dto.setCreatedAt(commentEntity.getCreatedAt());
        dto.setUpdatedAt(commentEntity.getUpdatedAt());
        return dto;
    }

    public CommentListResponse toCommentListResponse(Page<CommentEntity> commentPage) {
        CommentListResponse response = new CommentListResponse();
        List<Comment> content = commentPage.getContent().stream().map(this::toDto).toList();
        response.setContent(content);
        response.setPage(commentPage.getNumber());
        response.setSize(commentPage.getSize());
        response.setTotalElements((int) commentPage.getTotalElements());
        response.setTotalPages(commentPage.getTotalPages());
        return response;
    }
}
