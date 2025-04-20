import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager history;
    private Task task;

    @BeforeEach
    void setUp() {
        history = Managers.getDefaultHistory();
        task = new Task("Task", "Description");
        task.setId(1);
    }

    @Test
    void shouldAddTaskToHistory() {
        history.add(task);
        assertFalse(history.getHistory().isEmpty(), "История не должна быть пустой после добавления");
    }

    @Test
    void shouldLimitHistorySize() {
        for (int i = 1; i <= 15; i++) {
            Task t = new Task("Task " + i, "Description");
            t.setId(i);
            history.add(t);
        }
        assertEquals(10, history.getHistory().size(), "История должна ограничиваться 10 задачами");
    }
}