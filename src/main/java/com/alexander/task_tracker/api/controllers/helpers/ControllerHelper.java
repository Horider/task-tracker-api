package com.alexander.task_tracker.api.controllers.helpers;

import com.alexander.task_tracker.api.exceptions.NotFoundException;
import com.alexander.task_tracker.store.entities.ProjectEntity;
import com.alexander.task_tracker.store.repositories.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Component
@Transactional
public class ControllerHelper {

    ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
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
