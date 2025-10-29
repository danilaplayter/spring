/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.mentee.power.domain.model.TaskEntity;

@Repository
public interface TaskRepository
        extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {}
