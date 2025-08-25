package test;

import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTimeTest {
    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.createEpic(new Epic("Test Epic", "Description"));
    }

    @Test
    void testEpicTimeCalculation() {
        LocalDateTime now = LocalDateTime.now();

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStartTime(now.plusHours(1));
        subtask1.setDuration(Duration.ofMinutes(30));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStartTime(now.plusHours(2));
        subtask2.setDuration(Duration.ofMinutes(45));
        manager.createSubtask(subtask2);

        Epic savedEpic = manager.getEpic(epic.getId());

        assertEquals(now.plusHours(1), savedEpic.getStartTime());
        assertEquals(Duration.ofMinutes(75), savedEpic.getDuration());
        assertEquals(now.plusHours(2).plusMinutes(45), savedEpic.getEndTime());
    }

    @Test
    void testEpicTimeWithNoSubtasks() {
        Epic savedEpic = manager.getEpic(epic.getId());

        assertNull(savedEpic.getStartTime());
        assertNull(savedEpic.getDuration());
        assertNull(savedEpic.getEndTime());
    }
}