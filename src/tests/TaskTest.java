package tests;

import managers.Managers;
import managers.TaskManager;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("model.Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("model.Task 2", "Description");
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void taskShouldNotChangeAfterAddingToManager() {
        TaskManager manager = Managers.getDefault();
        Task originalTask = new Task("Original", "Description");
        originalTask.setStatus(TaskStatus.IN_PROGRESS);

        int taskId = manager.createTask(originalTask).getId();
        Task savedTask = manager.getTask(taskId);

        assertEquals("Original", savedTask.getTitle());
        assertEquals("Description", savedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());
    }
}