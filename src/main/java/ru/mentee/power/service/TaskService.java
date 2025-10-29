/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.api.generated.dto.*;
import ru.mentee.power.domain.mapper.TaskMapper;
import ru.mentee.power.domain.model.TaskEntity;
import ru.mentee.power.domain.repository.TaskRepository;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final ObjectMapper objectMapper;

    public TaskEntity createTask(TaskEntity entity) {
        log.info("Creating new task with title: {}", entity.getTitle());
        return taskRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public TaskEntity getTaskById(UUID id) {
        log.debug("Fetching task by id: {}", id);
        return taskRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
    }

    public TaskEntity updateTask(TaskEntity entity) {
        log.info("Updating task with id: {}", entity.getId());
        return taskRepository.save(entity);
    }

    public void deleteTaskById(UUID id) {
        log.info("Deleting task with id: {}", id);
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<TaskEntity> getTasksWithFilters(
            String status, String assignee, String priority, Pageable pageable) {
        log.debug(
                "Fetching tasks with filters - status: {}, assignee: {}, priority: {}",
                status,
                assignee,
                priority);

        Specification<TaskEntity> spec = createTaskSpecification(status, assignee, priority);
        return taskRepository.findAll(spec, pageable);
    }

    private Specification<TaskEntity> createTaskSpecification(
            String status, String assignee, String priority) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.trim().isEmpty()) {
                try {
                    TaskEntity.TaskStatus taskStatus =
                            TaskEntity.TaskStatus.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), taskStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: {}", status);
                }
            }

            if (assignee != null && !assignee.trim().isEmpty()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("assignee")),
                                "%" + assignee.toLowerCase() + "%"));
            }

            if (priority != null && !priority.trim().isEmpty()) {
                try {
                    TaskEntity.TaskPriority taskPriority =
                            TaskEntity.TaskPriority.valueOf(priority.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("priority"), taskPriority));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid priority filter: {}", priority);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public TaskEntity applyPatch(TaskEntity entity, List<JsonPatchOperation> patchOperations) {
        log.debug("Applying patch operations to task: {}", entity.getId());

        try {
            // Convert entity to JSON
            JsonNode entityNode = objectMapper.valueToTree(entity);

            // Apply each patch operation
            for (JsonPatchOperation operation : patchOperations) {
                entityNode = applyPatchOperation(entityNode, operation);
            }

            // Convert back to entity
            return objectMapper.treeToValue(entityNode, TaskEntity.class);
        } catch (Exception e) {
            log.error("Error applying patch operations: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid patch operations", e);
        }
    }

    private JsonNode applyPatchOperation(JsonNode node, JsonPatchOperation operation) {
        String path = operation.getPath().substring(1); // Remove leading '/'
        String[] pathParts = path.split("/");

        if (pathParts.length == 1) {
            String field = pathParts[0];
            ObjectNode objectNode = (ObjectNode) node;

            switch (operation.getOp()) {
                case REPLACE:
                    if (field.equals("status")) {
                        objectNode.put(field, operation.getValue());
                    } else if (field.equals("priority")) {
                        objectNode.put(field, operation.getValue());
                    } else if (field.equals("title")
                            || field.equals("description")
                            || field.equals("assignee")) {
                        objectNode.put(field, operation.getValue());
                    }
                    break;
                case ADD:
                    objectNode.put(field, operation.getValue());
                    break;
                case REMOVE:
                    objectNode.remove(field);
                    break;
                default:
                    log.warn("Unsupported patch operation: {}", operation.getOp());
            }
        }

        return node;
    }

    public Task addHateoasLinks(Task task) {
        if (task == null) {
            return null;
        }

        HalLinks links = new HalLinks();

        // Self link
        Link selfLink = new Link();
        selfLink.setHref("/api/v1/tasks/" + task.getId());
        selfLink.setMethod(Link.MethodEnum.GET);
        links.setSelf(selfLink);

        // Comments link
        Link commentsLink = new Link();
        commentsLink.setHref("/api/v1/tasks/" + task.getId() + "/comments");
        commentsLink.setMethod(Link.MethodEnum.GET);
        links.setComments(commentsLink);

        task.setLinks(links);
        return task;
    }
}
