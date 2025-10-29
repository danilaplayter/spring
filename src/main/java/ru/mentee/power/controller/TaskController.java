/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.mentee.power.api.generated.controller.TasksApi;
import ru.mentee.power.api.generated.dto.*;
import ru.mentee.power.domain.mapper.TaskMapper;
import ru.mentee.power.domain.model.TaskEntity;
import ru.mentee.power.service.TaskService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TaskController implements TasksApi {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Override
    @Operation(summary = "Создать новую задачу", description = "Создает новую задачу в системе")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                @ApiResponse(responseCode = "422", description = "Ошибка валидации данных"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Task> createTask(@Valid CreateTaskRequest createTaskRequest) {
        log.info("Creating new task with title: {}", createTaskRequest.getTitle());

        TaskEntity entity = taskMapper.toEntity(createTaskRequest);
        TaskEntity savedEntity = taskService.createTask(entity);
        Task response = taskMapper.toDto(savedEntity);

        response = taskService.addHateoasLinks(response);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(
                        "Location",
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(savedEntity.getId())
                                .toUriString())
                .body(response);
    }

    @Override
    @Operation(
            summary = "Получить задачу по ID",
            description = "Возвращает информацию о задаче по её идентификатору")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Информация о задаче"),
                @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "Идентификатор задачи", required = true) UUID taskId) {
        log.debug("Fetching task by id: {}", taskId);

        TaskEntity entity = taskService.getTaskById(taskId);
        Task response = taskMapper.toDto(entity);

        response = taskService.addHateoasLinks(response);

        return ResponseEntity.ok(response);
    }

    @Override
    @Operation(summary = "Обновить задачу", description = "Полностью обновляет информацию о задаче")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
                @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                @ApiResponse(responseCode = "422", description = "Ошибка валидации данных"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Task> updateTask(
            @Parameter(description = "Идентификатор задачи", required = true) UUID taskId,
            @Valid UpdateTaskRequest updateTaskRequest) {
        log.info("Updating task with id: {}", taskId);

        TaskEntity existingEntity = taskService.getTaskById(taskId);
        TaskEntity updateEntity = taskMapper.updateRequestToEntity(updateTaskRequest);
        taskMapper.updateEntityFromEntity(existingEntity, updateEntity);

        TaskEntity savedEntity = taskService.updateTask(existingEntity);
        Task response = taskMapper.toDto(savedEntity);

        response = taskService.addHateoasLinks(response);

        return ResponseEntity.ok(response);
    }

    @Override
    @Operation(
            summary = "Частично обновить задачу",
            description = "Применяет JSON Patch операции к задаче")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
                @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                @ApiResponse(responseCode = "422", description = "Ошибка валидации данных"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Void> patchTask(
            @Parameter(description = "Идентификатор задачи", required = true) UUID taskId,
            @Valid List<JsonPatchOperation> jsonPatchOperation) {
        log.info("Patching task with id: {}", taskId);

        TaskEntity entity = taskService.getTaskById(taskId);
        TaskEntity patchedEntity = taskService.applyPatch(entity, jsonPatchOperation);
        taskService.updateTask(patchedEntity);

        return ResponseEntity.ok().build();
    }

    @Override
    @Operation(summary = "Удалить задачу", description = "Удаляет задачу из системы")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
                @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Идентификатор задачи", required = true) UUID taskId) {
        log.info("Deleting task with id: {}", taskId);

        taskService.deleteTaskById(taskId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Operation(
            summary = "Получить список задач",
            description = "Возвращает список задач с поддержкой фильтрации, сортировки и пагинации")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
                @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<TaskListResponse> getTasks(
            @Parameter(description = "Фильтр по статусу задачи") String status,
            @Parameter(description = "Фильтр по исполнителю") String assignee,
            @Parameter(description = "Фильтр по приоритету") String priority,
            @Parameter(description = "Параметры сортировки в формате field:direction") String sort,
            @Parameter(description = "Номер страницы (начиная с 0)") Integer page,
            @Parameter(description = "Размер страницы") Integer size) {

        log.debug(
                "Fetching tasks with filters - status: {}, assignee: {}, priority: {}, sort: {}, page: {}, size: {}",
                status,
                assignee,
                priority,
                sort,
                page,
                size);

        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;

        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Pageable pageable = createPageable(pageNumber, pageSize, sort);

        Page<TaskEntity> taskPage =
                taskService.getTasksWithFilters(status, assignee, priority, pageable);

        TaskListResponse response = taskMapper.toTaskListResponse(taskPage);

        response.getContent().forEach(taskService::addHateoasLinks);

        return ResponseEntity.ok(response);
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return PageRequest.of(page, size, Sort.by("createdAt").descending());
        }

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.ASC;
            String property = "createdAt";

            if (sortParams.length > 0) {
                String[] firstSort = sortParams[0].split(":");
                if (firstSort.length == 2) {
                    property = firstSort[0];
                    direction =
                            "desc".equalsIgnoreCase(firstSort[1])
                                    ? Sort.Direction.DESC
                                    : Sort.Direction.ASC;
                }
            }

            return PageRequest.of(page, size, Sort.by(direction, property));
        } catch (Exception e) {
            log.warn("Invalid sort parameter: {}, using default sorting", sort);
            return PageRequest.of(page, size, Sort.by("createdAt").descending());
        }
    }
}
