package com.alexander.task_tracker.store.repositories;

import com.alexander.task_tracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TackStateRepository extends JpaRepository<TaskStateEntity, Long> {
}
