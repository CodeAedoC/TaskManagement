package com.taskmanager.backend.repository;

import com.taskmanager.backend.model.Task;
import com.taskmanager.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByUserAndIsDeletedFalse(User user, Pageable pageable);

    Page<Task> findByUserAndStatusAndIsDeletedFalse(User user, Task.Status status, Pageable pageable);

    Page<Task> findByUserAndPriorityAndIsDeletedFalse(User user, Task.Priority priority, Pageable pageable);

    Page<Task> findByUserAndStatusAndPriorityAndIsDeletedFalse(
            User user, Task.Status status, Task.Priority priority, Pageable pageable);

    Optional<Task> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT t FROM Task t WHERE t.user = :user AND t.isDeleted = false " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findTasksWithFilters(
            @Param("user") User user,
            @Param("status") Task.Status status,
            @Param("priority") Task.Priority priority,
            Pageable pageable);
}
