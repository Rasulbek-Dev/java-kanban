package managers;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager history;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        history = Managers.getDefaultHistory();

        task1 = new Task("Task 1", "Description");
        task1.setId(1);

        task2 = new Task("Task 2", "Description");
        task2.setId(2);

        task3 = new Task("Task 3", "Description");
        task3.setId(3);
    }

    @Test
    void shouldAddTaskToHistory() {
        history.add(task1);
        List<Task> historyList = history.getHistory();

        assertEquals(1, historyList.size());
        assertEquals(task1, historyList.get(0));
    }

    @Test
    void shouldRemoveDuplicatesFromHistory() {
        history.add(task1);
        history.add(task2);
        history.add(task1);

        List<Task> historyList = history.getHistory();

        assertEquals(2, historyList.size());
        assertEquals(task2, historyList.get(0));
        assertEquals(task1, historyList.get(1));
    }

    @Test
    void shouldRemoveFromBeginningOfHistory() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(task1.getId());
        List<Task> historyList = history.getHistory();
        assertEquals(2, historyList.size());
        assertEquals(task2, historyList.get(0));
        assertEquals(task3, historyList.get(1));
    }

    @Test
    void shouldRemoveFromMiddleOfHistory() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(task2.getId());

        List<Task> historyList = history.getHistory();
        assertEquals(2, historyList.size());
        assertEquals(task1, historyList.get(0));
        assertEquals(task3, historyList.get(1));
    }

    @Test
    void shouldRemoveFromEndOfHistory() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        history.remove(task3.getId());

        List<Task> historyList = history.getHistory();
        assertEquals(2, historyList.size());
        assertEquals(task1, historyList.get(0));
        assertEquals(task2, historyList.get(1));
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> historyList = history.getHistory();
        assertTrue(historyList.isEmpty());
    }

    @Test
    void shouldMaintainInsertionOrder() {
        history.add(task1);
        history.add(task2);
        history.add(task3);

        List<Task> historyList = history.getHistory();
        assertEquals(3, historyList.size());
        assertEquals(task1, historyList.get(0));
        assertEquals(task2, historyList.get(1));
        assertEquals(task3, historyList.get(2));
    }

    @Test
    void shouldHandleRemovingNonExistentTask() {
        history.add(task1);
        history.add(task2);

        history.remove(999);

        List<Task> historyList = history.getHistory();
        assertEquals(2, historyList.size());
    }
}