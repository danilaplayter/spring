/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.domain.model.CommentEntity;
import ru.mentee.power.domain.repository.CommentRepository;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentEntity createComment(CommentEntity entity) {
        log.info(
                "Creating new comment for task: {} by author: {}",
                entity.getTask().getId(),
                entity.getAuthor());
        return commentRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Page<CommentEntity> getCommentsByTaskId(UUID taskId, Pageable pageable) {
        log.debug("Fetching comments for task id: {}", taskId);
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
    }

    @Transactional(readOnly = true)
    public CommentEntity getCommentById(UUID id) {
        log.debug("Fetching comment by id: {}", id);
        return commentRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new jakarta.persistence.EntityNotFoundException(
                                        "Comment not found with id: " + id));
    }

    public void deleteCommentById(UUID id) {
        log.info("Deleting comment with id: {}", id);
        if (!commentRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException(
                    "Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }
}
