
package org.classflow;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TaskController {

    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public TaskController(
            TaskRepository taskRepository,
            TaskService taskService) {

        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<Task> getTasks() {

        return taskRepository.findAll();
    }

    @GetMapping("/tasks/{id}")
    public Task getTaskById(@PathVariable("id") Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found: " + id));
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Task task) {

        validateDependency(task.getDependsOnTaskId(), null);

        return taskRepository.save(task);
    }

    @PutMapping("/tasks/{id}")
    public Task updateTask(
            @PathVariable("id") Long id,
            @RequestBody Task updatedTask) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found: " + id));

        validateDependency(
                updatedTask.getDependsOnTaskId(), id);

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setDeadline(updatedTask.getDeadline());

        task.setPlannedStartDate(updatedTask.getPlannedStartDate());

        task.setActualStartDate(updatedTask.getActualStartDate());

        task.setPlannedCompletionDate(updatedTask.getPlannedCompletionDate());

        task.setActualCompletionDate( updatedTask.getActualCompletionDate());

        task.setDependsOnTaskId(updatedTask.getDependsOnTaskId());

        return taskRepository.save(task);
    }

    private void validateDependency(Long dependencyId, Long currentTaskId) {

        if (dependencyId == null) {
            return;
        }

        if (!taskRepository.existsById(dependencyId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dependency task not found: " + dependencyId);
        }

        if (currentTaskId != null && currentTaskId.equals(dependencyId)) {

            throw new ResponseStatusException( HttpStatus.BAD_REQUEST ,"A task cannot depend on itself");
        }
    }

    @GetMapping("/tasks/{id}/overdue")
    public boolean checkOverdue(@PathVariable("id") Long id) {

        Task task = getTaskById(id);

        return task.isOverdue();
    }

    @GetMapping("/tasks/{id}/deadline-warning")
    public String getDeadlineWarning(
            @PathVariable("id") Long id) {

        Task task = getTaskById(id);

        return task.getDeadlineWarning();
    }

    @GetMapping("/tasks/{id}/start-delay")
    public long getStartDelay(@PathVariable("id") Long id) {

        Task task = getTaskById(id);

        return task.calculateStartDelayDays();
    }

    @GetMapping("/tasks/{id}/start-delayed")
    public boolean checkStartDelayed(
            @PathVariable("id") Long id) {

        Task task = getTaskById(id);

        return task.isStartDelayed();
    }

    @GetMapping("/tasks/{id}/delay")
    public long getTaskDelay(@PathVariable("id") Long id) {

        Task task = getTaskById(id);

        return task.calculateDelayDays();
    }

    @GetMapping("/tasks/{id}/dependents")
    public List<Task> getDependentTasks(
            @PathVariable("id") Long id) {

        getTaskById(id);

        return taskService.getDependentTasks(id);
    }

    @GetMapping("/tasks/{id}/affected-tasks")
    public List<Task> getAffectedTasks(
            @PathVariable("id") Long id) {

        getTaskById(id);

        return taskService.getAllAffectedTasks(id);
    }

    @GetMapping("/tasks/{id}/impact")
    public Map<String, Object> getDelayImpact(
            @PathVariable("id") Long id) {

        return taskService.analyzeDelayImpact(id);
    }

    @GetMapping("/tasks/{id}/dependency")
    public String checkDependency(
            @PathVariable("id") Long id) {

        return taskService.checkDependency(id);
    }

    @DeleteMapping("/tasks/{id}")
    public String deleteTask(@PathVariable("id") Long id) {

        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + id);
        }

        taskRepository.delete(task);

        return "Task deleted successfully";
    }
}
