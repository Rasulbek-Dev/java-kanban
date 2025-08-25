package test;

import managers.FileBackedTaskManager;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTimeTest {
    @TempDir
    Path tempDir;
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = File.createTempFile("test_time", ".csv", tempDir.toFile());
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void testSaveAndLoadWithTime() {
        LocalDateTime now = LocalDateTime.now();

        Task task = new Task("Test Task", "Description");
        task.setStartTime(now);
        task.setDuration(Duration.ofMinutes(45));
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.createTask(task);

        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        subtask.setStartTime(now.plusHours(1));
        subtask.setDuration(Duration.ofMinutes(30));
        manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loaded.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals(now, tasks.get(0).getStartTime());
        assertEquals(Duration.ofMinutes(45), tasks.get(0).getDuration());

        List<Subtask> subtasks = loaded.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(now.plusHours(1), subtasks.get(0).getStartTime());

        List<Epic> epics = loaded.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals(now.plusHours(1), epics.get(0).getStartTime());
        assertEquals(Duration.ofMinutes(30), epics.get(0).getDuration());
    }
}