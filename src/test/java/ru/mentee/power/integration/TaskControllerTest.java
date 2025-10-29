/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.integration;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.api.generated.dto.*;
import ru.mentee.power.api.generated.dto.CreateTaskRequest.PriorityEnum;
import ru.mentee.power.api.generated.dto.UpdateTaskRequest.StatusEnum;
import ru.mentee.power.domain.model.TaskEntity;
import ru.mentee.power.domain.repository.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private TaskRepository taskRepository;

    @Autowired private ObjectMapper objectMapper;

    private TaskEntity testTask;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        taskId = UUID.randomUUID();
        testTask =
                TaskEntity.builder()
                        .id(taskId)
                        .title("Integration Test Task")
                        .description("Test Description for Integration Test")
                        .status(TaskEntity.TaskStatus.TODO)
                        .priority(TaskEntity.TaskPriority.MEDIUM)
                        .assignee("integration.user")
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();

        testTask = taskRepository.save(testTask);
    }

    @Test
    void createTask_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Integration Task");
        request.setDescription("Task created in integration test");
        request.setPriority(PriorityEnum.HIGH);
        request.setAssignee("test.user");
        request.setDueDate(OffsetDateTime.now().plusDays(7));
        request.setTags(Arrays.asList("urgent", "test"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Integration Task"))
                .andExpect(jsonPath("$.description").value("Task created in integration test"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assignee").value("test.user"))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.comments").exists());

        // Verify in database
        assertEquals(2, taskRepository.count());
    }

    @Test
    void createTask_WithRequiredFieldsOnly_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Minimal Task");
        request.setPriority(PriorityEnum.MEDIUM);

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Minimal Task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO")) // Default value
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void createTask_WithInvalidData_ShouldReturnUnprocessableEntity() throws Exception {
        // Arrange - title too short
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("ab"); // Less than minLength: 3
        request.setPriority(PriorityEnum.MEDIUM);

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()) // 422 вместо 400
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].field").value("title"));

        assertEquals(1, taskRepository.count());
    }

    @Test
    void createTask_WithMissingRequiredFields_ShouldReturnUnprocessableEntity() throws Exception {
        // Arrange - missing priority (required field)
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task without priority");

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()) // 422 вместо 400
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                .andExpect(jsonPath("$.validationErrors[0].field").value("priority"))
                .andExpect(jsonPath("$.validationErrors[0].message").exists())
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").isEmpty());
    }

    @Test
    void getTaskById_WhenExists_ShouldReturnTask() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId().toString()))
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description for Integration Test"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.assignee").value("integration.user"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.comments").exists());
    }

    @Test
    void getTaskById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateTask_WithValidData_ShouldReturnUpdatedTask() throws Exception {
        // Arrange
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Integration Task");
        request.setDescription("Updated description");
        request.setStatus(StatusEnum.IN_PROGRESS);
        request.setPriority(UpdateTaskRequest.PriorityEnum.CRITICAL);
        request.setAssignee("updated.user");
        request.setTags(Arrays.asList("updated", "critical"));

        // Act & Assert
        mockMvc.perform(
                        put("/api/v1/tasks/{id}", testTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId().toString()))
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.assignee").value("updated.user"))
                .andExpect(jsonPath("$.tags", hasSize(2)));

        // Verify in database
        TaskEntity updatedEntity = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals("Updated Integration Task", updatedEntity.getTitle());
        assertEquals(TaskEntity.TaskStatus.IN_PROGRESS, updatedEntity.getStatus());
        assertEquals(TaskEntity.TaskPriority.CRITICAL, updatedEntity.getPriority());
    }

    @Test
    void updateTask_WithPartialData_ShouldUpdateOnlyProvidedFields() throws Exception {
        // Arrange - update only title
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Only Title Updated");

        // Act & Assert
        mockMvc.perform(
                        put("/api/v1/tasks/{id}", testTask.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Only Title Updated"))
                .andExpect(
                        jsonPath("$.description")
                                .value("Test Description for Integration Test")) // Should remain
                .andExpect(jsonPath("$.status").value("TODO")) // Should remain
                .andExpect(jsonPath("$.priority").value("MEDIUM")); // Should remain
    }

    @Test
    void updateTask_WhenTaskNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setStatus(StatusEnum.DONE);

        // Act & Assert
        mockMvc.perform(
                        put("/api/v1/tasks/{id}", nonExistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchTask_WithValidReplaceOperations_ShouldUpdateTask() throws Exception {
        // Arrange
        List<JsonPatchOperation> patchOperations =
                Arrays.asList(
                        createPatchOperation("replace", "/title", "Patched Title"),
                        createPatchOperation("replace", "/status", "DONE"),
                        createPatchOperation("replace", "/assignee", "patched.user"));

        // Act & Assert
        mockMvc.perform(
                        patch("/api/v1/tasks/{id}", testTask.getId())
                                .contentType("application/json-patch+json")
                                .content(objectMapper.writeValueAsString(patchOperations)))
                .andExpect(status().isOk());

        // Verify in database
        TaskEntity patchedEntity = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals("Patched Title", patchedEntity.getTitle());
        assertEquals(TaskEntity.TaskStatus.DONE, patchedEntity.getStatus());
        assertEquals("patched.user", patchedEntity.getAssignee());
    }

    @Test
    void patchTask_WithRemoveOperation_ShouldRemoveField() throws Exception {
        // Arrange
        List<JsonPatchOperation> patchOperations =
                Arrays.asList(createPatchOperation("remove", "/description", null));

        // Act & Assert
        mockMvc.perform(
                        patch("/api/v1/tasks/{id}", testTask.getId())
                                .contentType("application/json-patch+json")
                                .content(objectMapper.writeValueAsString(patchOperations)))
                .andExpect(status().isOk());

        // Verify description was removed
        TaskEntity patchedEntity = taskRepository.findById(testTask.getId()).orElseThrow();
        assertNull(patchedEntity.getDescription());
    }

    @Test
    void deleteTask_WhenExists_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/tasks/{id}", testTask.getId()))
                .andExpect(status().isNoContent());

        // Verify in database
        assertFalse(taskRepository.existsById(testTask.getId()));
        assertEquals(0, taskRepository.count());
    }

    @Test
    void deleteTask_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTasks_WithoutFilters_ShouldReturnAllTasks() throws Exception {
        // Arrange - Create additional tasks
        createTestTask(
                "Second Task",
                TaskEntity.TaskStatus.IN_PROGRESS,
                TaskEntity.TaskPriority.HIGH,
                "user2");
        createTestTask(
                "Third Task", TaskEntity.TaskStatus.DONE, TaskEntity.TaskPriority.LOW, "user3");

        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content[0]._links.self").exists())
                .andExpect(jsonPath("$.content[0]._links.comments").exists());
    }

    @Test
    void getTasks_WithStatusFilter_ShouldReturnFilteredTasks() throws Exception {
        // Arrange
        createTestTask(
                "In Progress Task",
                TaskEntity.TaskStatus.IN_PROGRESS,
                TaskEntity.TaskPriority.MEDIUM,
                "user1");
        createTestTask(
                "Done Task", TaskEntity.TaskStatus.DONE, TaskEntity.TaskPriority.HIGH, "user2");

        // Act & Assert - Filter by TODO status
        mockMvc.perform(get("/api/v1/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[0].title").value("Integration Test Task"));
    }

    @Test
    void getTasks_WithAssigneeFilter_ShouldReturnFilteredTasks() throws Exception {
        // Arrange
        createTestTask(
                "John's Task",
                TaskEntity.TaskStatus.TODO,
                TaskEntity.TaskPriority.MEDIUM,
                "john.doe");
        createTestTask(
                "Jane's Task",
                TaskEntity.TaskStatus.IN_PROGRESS,
                TaskEntity.TaskPriority.HIGH,
                "jane.smith");

        // Act & Assert - Filter by assignee containing "john"
        mockMvc.perform(get("/api/v1/tasks").param("assignee", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].assignee").value("john.doe"));
    }

    @Test
    void getTasks_WithPriorityFilter_ShouldReturnFilteredTasks() throws Exception {
        // Arrange
        createTestTask(
                "High Priority Task",
                TaskEntity.TaskStatus.TODO,
                TaskEntity.TaskPriority.HIGH,
                "user1");
        createTestTask(
                "Critical Priority Task",
                TaskEntity.TaskStatus.IN_PROGRESS,
                TaskEntity.TaskPriority.CRITICAL,
                "user2");

        // Act & Assert - Filter by HIGH priority
        mockMvc.perform(get("/api/v1/tasks").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[0].title").value("High Priority Task"));
    }

    @Test
    void getTasks_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        // Arrange - Create multiple tasks
        for (int i = 1; i <= 5; i++) {
            createTestTask(
                    "Task " + i,
                    TaskEntity.TaskStatus.TODO,
                    TaskEntity.TaskPriority.MEDIUM,
                    "user" + i);
        }

        // Act & Assert - Request first page with 3 items
        mockMvc.perform(get("/api/v1/tasks").param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(6)) // 5 new + 1 existing
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(3));
    }

    @Test
    void getTasks_WithSorting_ShouldReturnSortedResults() throws Exception {
        // Arrange - Create tasks with different titles
        createTestTask(
                "Alpha Task", TaskEntity.TaskStatus.TODO, TaskEntity.TaskPriority.MEDIUM, "user1");
        createTestTask(
                "Zulu Task", TaskEntity.TaskStatus.TODO, TaskEntity.TaskPriority.MEDIUM, "user2");

        // Act & Assert - Sort by title descending
        mockMvc.perform(get("/api/v1/tasks").param("sort", "title:desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Zulu Task"))
                .andExpect(jsonPath("$.content[1].title").value("Integration Test Task"))
                .andExpect(jsonPath("$.content[2].title").value("Alpha Task"));
    }

    @Test
    void getTasks_WithMultipleFilters_ShouldReturnCorrectlyFilteredTasks() throws Exception {
        // Arrange
        createTestTask(
                "Filtered Task",
                TaskEntity.TaskStatus.IN_PROGRESS,
                TaskEntity.TaskPriority.HIGH,
                "specific.user");

        // Act & Assert - Combine multiple filters
        mockMvc.perform(
                        get("/api/v1/tasks")
                                .param("status", "IN_PROGRESS")
                                .param("priority", "HIGH")
                                .param("assignee", "specific"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Filtered Task"))
                .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[0].assignee").value("specific.user"));
    }

    @Test
    void getTasks_WithInvalidStatus_ShouldIgnoreInvalidFilter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/tasks").param("status", "INVALID_STATUS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getAllEndpoints_ShouldIncludeHateoasLinks() throws Exception {
        // Test that all task responses include HATEOAS links
        mockMvc.perform(get("/api/v1/tasks/{id}", testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(
                        jsonPath("$._links.self.href")
                                .value(containsString("/api/v1/tasks/" + testTask.getId())))
                .andExpect(jsonPath("$._links.self.method").value("GET"))
                .andExpect(jsonPath("$._links.comments").exists())
                .andExpect(jsonPath("$._links.comments.href").value(containsString("/comments")))
                .andExpect(jsonPath("$._links.comments.method").value("GET"));
    }

    // Helper methods
    private JsonPatchOperation createPatchOperation(String op, String path, String value) {
        JsonPatchOperation operation = new JsonPatchOperation();
        operation.setOp(JsonPatchOperation.OpEnum.fromValue(op));
        operation.setPath(path);
        operation.setValue(value);
        return operation;
    }

    private void createTestTask(
            String title,
            TaskEntity.TaskStatus status,
            TaskEntity.TaskPriority priority,
            String assignee) {
        TaskEntity task =
                TaskEntity.builder()
                        .title(title)
                        .status(status)
                        .priority(priority)
                        .assignee(assignee)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build();
        taskRepository.save(task);
    }
}
