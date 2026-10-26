package org.classflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getDependentTasks(Long taskId) {

        List<Task> allTasks = taskRepository.findAll();
        List<Task> dependentTasks = new ArrayList<>();

        for (Task task : allTasks) {

            if (task.getDependsOnTaskId() != null
                    && task.getDependsOnTaskId().equals(taskId)) {

                dependentTasks.add(task);
            }
        }

        return dependentTasks;
    }

    public List<Task> getAllAffectedTasks(Long taskId) {

        List<Task> allTasks = taskRepository.findAll();
        List<Task> affectedTasks = new ArrayList<>();

        Set<Long> visitedTaskIds = new HashSet<>();
        List<Long> tasksToCheck = new ArrayList<>();

        visitedTaskIds.add(taskId);
        tasksToCheck.add(taskId);

        int index = 0;

        while (index < tasksToCheck.size()) {

            Long currentTaskId = tasksToCheck.get(index);
            index++;

            for (Task task : allTasks) {

                Long dependencyId = task.getDependsOnTaskId();

                if (dependencyId != null
                        && dependencyId.equals(currentTaskId)
                        && !visitedTaskIds.contains(task.getId())) {

                    visitedTaskIds.add(task.getId());
                    tasksToCheck.add(task.getId());

                    if (!task.isCompleted()) {
                        affectedTasks.add(task);
                    }
                }
            }
        }

        return affectedTasks;
    }

    public Map<String, Object> analyzeDelayImpact(Long taskId) {

        Map<String, Object> report = new HashMap<>();

        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            report.put("message", "Task not found");
            return report;
        }

        List<Task> directDependentTasks =
                getDependentTasks(taskId);

        List<Task> affectedTasks =
                getAllAffectedTasks(taskId);

        long delayDays = task.calculateDelayDays();

        report.put("taskId", task.getId());
        report.put("taskTitle", task.getTitle());
        report.put("taskStatus", task.getStatus());
        report.put("plannedCompletionDate",task.getPlannedCompletionDate());
        report.put("actualCompletionDate",task.getActualCompletionDate());
        report.put("delayDays", delayDays);
        report.put("overdue", task.isOverdue());

        report.put("directDependentTasksCount", directDependentTasks.size());

        report.put("affectedTasksCount",affectedTasks.size());

        report.put("affectedTasks", affectedTasks);

        if (task.isCompleted() && delayDays == 0) {

            report.put("impactMessage",
                    "Task completed on time or before schedule");

        } else if (delayDays > 0 && !affectedTasks.isEmpty()) {

            report.put("impactMessage",
                    "Warning: Task is delayed and dependent tasks may be affected");

        } else if (delayDays > 0) {

            report.put("impactMessage",
                    "Task is delayed, but no incomplete dependent tasks were found");

        } else if (!affectedTasks.isEmpty()) {

            report.put("impactMessage",
                    "Incomplete dependent tasks found. Check their dependencies and status");

        } else {

            report.put("impactMessage",
                    "No incomplete dependent tasks found");
        }

        return report;
    }

    public String checkDependency(Long taskId) {

        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            return "Task not found";
        }

        if (task.getDependsOnTaskId() == null) {
            return "This task has no dependency";
        }

        Task dependencyTask = taskRepository
                .findById(task.getDependsOnTaskId())
                .orElse(null);

        if (dependencyTask == null) {
            return "Dependency task not found";
        }

        if (!dependencyTask.isCompleted()) {

            return "BLOCKED: Complete task ID "
                    + dependencyTask.getId()
                    + " before starting "
                    + task.getTitle();
        }

        return "Dependency completed. You can start "
                + task.getTitle();
    }

    public boolean dependencyExists(Long taskId) {
        return taskRepository.existsById(taskId);
    }
}
