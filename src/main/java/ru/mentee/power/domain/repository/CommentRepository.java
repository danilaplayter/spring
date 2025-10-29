/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mentee.power.domain.model.CommentEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    Page<CommentEntity> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable);
}
