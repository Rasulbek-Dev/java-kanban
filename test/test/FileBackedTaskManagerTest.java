package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        // Чтобы файл был пустым
        Files.writeString(tempFile.toPath(), "");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void shouldSaveAndLoadSingleTask() {
        Task task = new Task("Test Task", "Description");
        task.setStatus(TaskStatus.IN_PROGRESS);

        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loaded.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, tasks.get(0).getStatus());
    }

    @Test
    void shouldSaveAndLoadEpicWithSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "Sub desc", epic.getId());
        sub1.setStatus(TaskStatus.DONE);
        manager.createSubtask(sub1);

        Subtask sub2 = new Subtask("Sub 2", "Sub desc", epic.getId());
        manager.createSubtask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Epic> epics = loaded.getAllEpics();
        List<Subtask> subtasks = loaded.getAllSubtasks();

        assertEquals(1, epics.size());
        assertEquals("Epic 1", epics.get(0).getTitle());
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getTitle().equals("Sub 1") && s.getStatus() == TaskStatus.DONE));
    }

    @Test
    void shouldHandleEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldIncrementIdAfterLoading() {
        Task task1 = new Task("T1", "desc");
        manager.createTask(task1);

        Task task2 = new Task("T2", "desc");
        manager.createTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = new Task("T3", "desc");
        loaded.createTask(newTask);

        assertTrue(newTask.getId() > task2.getId());
    }

    @Test
    void shouldThrowExceptionOnCorruptedFile() throws IOException {
        Files.writeString(tempFile.toPath(),
                "id,type,name,status,description,epic\n" +
                        "broken,line,without,correct,fields");

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tempFile));
    }
}