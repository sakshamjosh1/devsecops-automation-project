package com.saksham.devsecops.service;

import com.saksham.devsecops.exception.ResourceNotFoundException;
import com.saksham.devsecops.model.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);

    public List<Task> getAllTasks() {
        return tasks;
    }

    public Task getTaskById(Long id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task with ID " + id + " not found"));
    }

    public Task createTask(Task task) {
        task.setId(counter.getAndIncrement());
        tasks.add(task);
        return task;
    }

    public Task updateTask(Long id, Task updatedTask) {

        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());

        return existingTask;
    }

    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        tasks.remove(task);
    }

}