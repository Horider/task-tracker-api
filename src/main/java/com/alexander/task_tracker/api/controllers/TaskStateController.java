package com.alexander.task_tracker.api.controllers;


import com.alexander.task_tracker.api.dto.ProjectDto;
import com.alexander.task_tracker.api.factories.TaskStateDtoFactory;
import com.alexander.task_tracker.store.entities.ProjectEntity;
import com.alexander.task_tracker.store.repositories.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
@RestController
public class TaskStateController {

    TaskStateDtoFactory taskStateDtoFactory;
    TaskStateRepository taskStateRepository;

    public static final String GET_TASK_STATE = "api/projects";
    public static final String DELETE_PROJECT = "api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT = "api/projects";

    @GetMapping(GET_TASK_STATE)
    public List<ProjectDto> fetchProject(@RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

}
