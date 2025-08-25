package test;

import managers.Managers;
import managers.TaskManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedTasksTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void testPrioritizedTasksOrder() {
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.now().plusHours(2));
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.now().plusHours(1));
        task2.setDuration(Duration.ofMinutes(45));
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals("Task 2", prioritized.get(0).getTitle());
        assertEquals("Task 1", prioritized.get(1).getTitle());
    }

    @Test
    void testTasksWithoutTimeNotInPrioritized() {
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.now().plusHours(1));
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(1, prioritized.size());
        assertEquals("Task 1", prioritized.get(0).getTitle());
    }
}