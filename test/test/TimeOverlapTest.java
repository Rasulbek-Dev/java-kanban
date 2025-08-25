package test;

import managers.ManagerValidationException;
import managers.Managers;
import managers.TaskManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeOverlapTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void testTimeOverlapDetection() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusMinutes(15));
        task2.setDuration(Duration.ofMinutes(30));

        assertThrows(ManagerValidationException.class, () -> manager.createTask(task2));
    }

    @Test
    void testNoTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusHours(1));
        task2.setDuration(Duration.ofMinutes(30));

        assertDoesNotThrow(() -> manager.createTask(task2));
    }
}