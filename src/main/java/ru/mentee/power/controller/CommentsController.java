/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.mentee.power.domain.mapper.CommentMapper;
import ru.mentee.power.domain.model.CommentEntity;
import ru.mentee.power.dto.Comment;
import ru.mentee.power.dto.CommentListResponse;
import ru.mentee.power.dto.CreateCommentRequest;
import ru.mentee.power.service.CommentService;
import ru.mentee.power.service.TaskService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CommentsController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final TaskService taskService;

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(
            summary = "Получить комментарии к задаче",
            description = "Возвращает список комментариев для указанной задачи")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Список комментариев успешно получен"),
                @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<CommentListResponse> getTaskComments(
            @Parameter(description = "Идентификатор задачи", required = true) UUID taskId,
            @Parameter(description = "Номер страницы (начиная с 0)") Integer page,
            @Parameter(description = "Размер страницы") Integer size) {

        log.debug("Fetching comments for task id: {}, page: {}, size: {}", taskId, page, size);

        taskService.getTaskById(taskId);

        int pageNumber = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;

        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<CommentEntity> commentPage = commentService.getCommentsByTaskId(taskId, pageable);

        CommentListResponse response = commentMapper.toCommentListResponse(commentPage);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}/comments")
    @Operation(
            summary = "Создать комментарий к задаче",
            description = "Создает новый комментарий для указанной задачи")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Комментарий успешно создан"),
                @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
                @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                @ApiResponse(responseCode = "422", description = "Ошибка валидации данных"),
                @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Comment> createTaskComment(
            @Parameter(description = "Идентификатор задачи", required = true) UUID taskId,
            @Valid CreateCommentRequest createCommentRequest) {

        log.info(
                "Creating comment for task id: {} by author: {}",
                taskId,
                createCommentRequest.getAuthor());

        taskService.getTaskById(taskId);

        CommentEntity entity = commentMapper.toEntity(createCommentRequest, taskId);
        CommentEntity savedEntity = commentService.createComment(entity);
        Comment response = commentMapper.toDto(savedEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(
                        "Location",
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(savedEntity.getId())
                                .toUriString())
                .body(response);
    }
}
