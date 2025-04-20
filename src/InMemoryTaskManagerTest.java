import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldCreateTaskWithGeneratedId() {
        Task task = manager.createTask(new Task("Task", "Description"));
        assertTrue(task.getId() > 0, "Задача должна создаваться с ID > 0");
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", 999));
        assertNull(subtask, "Нельзя создать подзадачу без существующего эпика");
    }
}