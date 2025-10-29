/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.mentee.power.api.generated.dto.*;
import ru.mentee.power.domain.mapper.TaskMapper;
import ru.mentee.power.domain.model.TaskEntity;
import ru.mentee.power.domain.repository.TaskRepository;
import ru.mentee.power.service.TaskService;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;

    @Mock private TaskMapper taskMapper;

    @Mock private ObjectMapper objectMapper;

    @InjectMocks private TaskService taskService;

    private TaskEntity taskEntity;
    private UUID taskId;
    private Task taskDto;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        taskEntity =
                TaskEntity.builder()
                        .id(taskId)
                        .title("Test Task")
                        .description("Test Description")
                        .assignee("john.doe")
                        .status(TaskEntity.TaskStatus.TODO)
                        .priority(TaskEntity.TaskPriority.MEDIUM)
                        .dueDate(OffsetDateTime.now().plusDays(7))
                        .tags(Arrays.asList("urgent", "backend"))
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();

        taskDto = new Task();
        taskDto.setId(taskId);
        taskDto.setTitle("Test Task");
    }

    @Test
    void createTask_ShouldSaveAndReturnEntity() {
        // Arrange
        when(taskRepository.save(taskEntity)).thenReturn(taskEntity);

        // Act
        TaskEntity result = taskService.createTask(taskEntity);

        // Assert
        assertNotNull(result);
        assertEquals(taskEntity, result);
        verify(taskRepository).save(taskEntity);
    }

    @Test
    void getTaskById_WhenExists_ShouldReturnEntity() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskEntity));

        // Act
        TaskEntity result = taskService.getTaskById(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskEntity, result);
        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(taskId));
        assertEquals("Task not found with id: " + taskId, exception.getMessage());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void updateTask_ShouldSaveAndReturnUpdatedEntity() {
        // Arrange
        TaskEntity updatedEntity =
                TaskEntity.builder()
                        .id(taskId)
                        .title("Updated Task")
                        .status(TaskEntity.TaskStatus.IN_PROGRESS)
                        .priority(TaskEntity.TaskPriority.HIGH)
                        .build();

        when(taskRepository.save(updatedEntity)).thenReturn(updatedEntity);

        // Act
        TaskEntity result = taskService.updateTask(updatedEntity);

        // Assert
        assertNotNull(result);
        assertEquals(updatedEntity, result);
        verify(taskRepository).save(updatedEntity);
    }

    @Test
    void deleteTaskById_WhenExists_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.existsById(taskId)).thenReturn(true);

        // Act
        taskService.deleteTaskById(taskId);

        // Assert
        verify(taskRepository).existsById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTaskById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(taskRepository.existsById(taskId)).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception =
                assertThrows(
                        EntityNotFoundException.class, () -> taskService.deleteTaskById(taskId));
        assertEquals("Task not found with id: " + taskId, exception.getMessage());
        verify(taskRepository).existsById(taskId);
        verify(taskRepository, never()).deleteById(taskId);
    }

    @Test
    void getTasksWithFilters_WithAllValidFilters_ShouldReturnFilteredResults() {
        // Arrange
        String status = "IN_PROGRESS";
        String assignee = "john";
        String priority = "HIGH";
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = Arrays.asList(taskEntity);
        Page<TaskEntity> expectedPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<TaskEntity> result =
                taskService.getTasksWithFilters(status, assignee, priority, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(taskEntity, result.getContent().get(0));
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getTasksWithFilters_WithAllTaskStatusValues_ShouldHandleCorrectly() {
        // Test all possible status values
        String[] statuses = {"TODO", "IN_PROGRESS", "DONE"};
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = Arrays.asList(taskEntity);

        for (String status : statuses) {
            // Arrange
            Page<TaskEntity> expectedPage = new PageImpl<>(tasks, pageable, tasks.size());
            when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<TaskEntity> result = taskService.getTasksWithFilters(status, null, null, pageable);

            // Assert
            assertNotNull(result);
            verify(taskRepository).findAll(any(Specification.class), eq(pageable));
            reset(taskRepository);
        }
    }

    @Test
    void getTasksWithFilters_WithAllPriorityValues_ShouldHandleCorrectly() {
        // Test all possible priority values
        String[] priorities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = Arrays.asList(taskEntity);

        for (String priority : priorities) {
            // Arrange
            Page<TaskEntity> expectedPage = new PageImpl<>(tasks, pageable, tasks.size());
            when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(expectedPage);

            // Act
            Page<TaskEntity> result =
                    taskService.getTasksWithFilters(null, null, priority, pageable);

            // Assert
            assertNotNull(result);
            verify(taskRepository).findAll(any(Specification.class), eq(pageable));
            reset(taskRepository);
        }
    }

    @Test
    void getTasksWithFilters_WithInvalidStatus_ShouldIgnoreInvalidFilter() {
        // Arrange
        String invalidStatus = "INVALID_STATUS";
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = Arrays.asList(taskEntity);
        Page<TaskEntity> expectedPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<TaskEntity> result =
                taskService.getTasksWithFilters(invalidStatus, null, null, pageable);

        // Assert
        assertNotNull(result);
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getTasksWithFilters_WithInvalidPriority_ShouldIgnoreInvalidFilter() {
        // Arrange
        String invalidPriority = "INVALID_PRIORITY";
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = Arrays.asList(taskEntity);
        Page<TaskEntity> expectedPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<TaskEntity> result =
                taskService.getTasksWithFilters(null, null, invalidPriority, pageable);

        // Assert
        assertNotNull(result);
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getTasksWithFilters_WithEmptyAndNullFilters_ShouldReturnAllResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<TaskEntity> tasks = Arrays.asList(taskEntity);
        Page<TaskEntity> expectedPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act - test various combinations of empty/null filters
        Page<TaskEntity> result1 = taskService.getTasksWithFilters(null, null, null, pageable);
        Page<TaskEntity> result2 = taskService.getTasksWithFilters("", "   ", "", pageable);
        Page<TaskEntity> result3 = taskService.getTasksWithFilters(" ", null, " ", pageable);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        verify(taskRepository, times(3)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void addHateoasLinks_WithValidTask_ShouldAddAllLinks() {
        // Arrange
        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");

        // Act
        Task result = taskService.addHateoasLinks(task);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLinks());

        // Verify self link
        assertNotNull(result.getLinks().getSelf());
        assertEquals("/api/v1/tasks/" + taskId, result.getLinks().getSelf().getHref());
        assertEquals(Link.MethodEnum.GET, result.getLinks().getSelf().getMethod());

        // Verify comments link
        assertNotNull(result.getLinks().getComments());
        assertEquals(
                "/api/v1/tasks/" + taskId + "/comments", result.getLinks().getComments().getHref());
        assertEquals(Link.MethodEnum.GET, result.getLinks().getComments().getMethod());
    }

    @Test
    void addHateoasLinks_WithNullTask_ShouldReturnNull() {
        // Act & Assert
        assertNull(taskService.addHateoasLinks(null));
    }

    @Test
    void addHateoasLinks_WithTaskWithoutId_ShouldCreateLinksWithNullId() {
        // Arrange
        Task task = new Task();
        task.setTitle("Task without ID");

        // Act
        Task result = taskService.addHateoasLinks(task);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLinks());
        assertNotNull(result.getLinks().getSelf());
        assertEquals("/api/v1/tasks/null", result.getLinks().getSelf().getHref());
    }

    @Test
    void createTask_WithFullEntityData_ShouldPersistAllFields() {
        // Arrange
        TaskEntity fullEntity =
                TaskEntity.builder()
                        .id(taskId)
                        .title("Complete Task")
                        .description("Full description with all details")
                        .status(TaskEntity.TaskStatus.IN_PROGRESS)
                        .priority(TaskEntity.TaskPriority.CRITICAL)
                        .assignee("full.user")
                        .dueDate(OffsetDateTime.now().plusDays(1))
                        .tags(Arrays.asList("critical", "release", "hotfix"))
                        .build();

        when(taskRepository.save(fullEntity)).thenReturn(fullEntity);

        // Act
        TaskEntity result = taskService.createTask(fullEntity);

        // Assert
        assertNotNull(result);
        assertEquals(fullEntity, result);
        assertEquals(TaskEntity.TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskEntity.TaskPriority.CRITICAL, result.getPriority());
        assertEquals(3, result.getTags().size());
        verify(taskRepository).save(fullEntity);
    }

    @Test
    void updateTask_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        TaskEntity partialUpdate =
                TaskEntity.builder().id(taskId).title("Updated Title Only").build();

        when(taskRepository.save(partialUpdate)).thenReturn(partialUpdate);

        // Act
        TaskEntity result = taskService.updateTask(partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title Only", result.getTitle());
        verify(taskRepository).save(partialUpdate);
    }

    // Helper method to create patch operations
    private JsonPatchOperation createPatchOperation(String op, String path, String value) {
        JsonPatchOperation operation = new JsonPatchOperation();
        operation.setOp(JsonPatchOperation.OpEnum.fromValue(op));
        operation.setPath(path);
        operation.setValue(value);
        return operation;
    }
}
