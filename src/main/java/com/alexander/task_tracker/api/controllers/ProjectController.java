package com.alexander.task_tracker.api.controllers;

import com.alexander.task_tracker.api.dto.AckDto;
import com.alexander.task_tracker.api.dto.ProjectDto;
import com.alexander.task_tracker.api.exceptions.BadRequestException;
import com.alexander.task_tracker.api.exceptions.NotFoundException;
import com.alexander.task_tracker.api.factories.ProjectDtoFactory;
import com.alexander.task_tracker.store.entities.ProjectEntity;
import com.alexander.task_tracker.store.repositories.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
@RestController
public class ProjectController {
    ProjectDtoFactory projectDtoFactory;
    ProjectRepository projectRepository;


    public static final String FETCH_PROJECT = "api/projects";
    public static final String DELETE_PROJECT = "api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT = "api/projects";


    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProject(@RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAll);

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName
    ) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreated = optionalProjectId.isEmpty();

        if (isCreated && optionalProjectName.isEmpty()) {
            throw new BadRequestException("Project can't be name empty");
        }

        final ProjectEntity projectEntity = optionalProjectId
                .map(this::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository.findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectEntity.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(
                                        String.format(
                                                "Project \"%s\" already exists.",
                                                projectName
                                        )
                                );
                            });
                    projectEntity.setName(projectName);
                });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(projectEntity);

        return projectDtoFactory.makeProjectDto(savedProject);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto deleteProject(@PathVariable("project_id") Long projectId) {

        getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }

    private ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project \"%s\" doesn't exists.",
                                        projectId
                                )
                        )
                );
    }
}
