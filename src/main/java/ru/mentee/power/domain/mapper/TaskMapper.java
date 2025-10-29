/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import ru.mentee.power.api.generated.dto.CreateTaskRequest;
import ru.mentee.power.api.generated.dto.Task;
import ru.mentee.power.api.generated.dto.TaskListResponse;
import ru.mentee.power.api.generated.dto.UpdateTaskRequest;
import ru.mentee.power.domain.model.TaskEntity;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "status", constant = "TODO")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    TaskEntity toEntity(CreateTaskRequest createTaskRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    TaskEntity updateRequestToEntity(UpdateTaskRequest updateTaskRequest);

    Task toDto(TaskEntity taskEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateEntityFromEntity(@MappingTarget TaskEntity target, TaskEntity source);

    @Mapping(target = "content", source = "content")
    @Mapping(target = "page", source = "number")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "totalElements", source = "totalElements")
    @Mapping(target = "totalPages", source = "totalPages")
    TaskListResponse toTaskListResponse(Page<TaskEntity> taskPage);
}
