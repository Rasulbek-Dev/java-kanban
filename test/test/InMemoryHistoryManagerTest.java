package test;

import managers.HistoryManager;
import managers.Managers;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager history;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        history = Managers.getDefaultHistory();
        task1 = new Task("Task 1", "Description");
        task1.setId(1);
        task2 = new Task("Task 2", "Description");
        task2.setId(2);
    }

    @Test
    void shouldAddTaskToHistory() {
        history.add(task1);
        List<Task> historyList = history.getHistory();
        assertEquals(1, historyList.size());
        assertEquals(task1, historyList.get(0));
    }

    @Test
    void shouldRemoveDuplicates() {
        history.add(task1);
        history.add(task2);
        history.add(task1);

        List<Task> historyList = history.getHistory();
        assertEquals(2, historyList.size());
        assertEquals(task2, historyList.get(0));
        assertEquals(task1, historyList.get(1));
    }

    @Test
    void shouldRemoveFromHistoryWhenDeleted() {
        history.add(task1);
        history.add(task2);
        history.remove(task1.getId());

        List<Task> historyList = history.getHistory();
        assertEquals(1, historyList.size());
        assertEquals(task2, historyList.get(0));
    }

    @Test
    void shouldMaintainInsertionOrder() {
        history.add(task1);
        history.add(task2);

        List<Task> historyList = history.getHistory();
        assertEquals(2, historyList.size());
        assertEquals(task1, historyList.get(0));
        assertEquals(task2, historyList.get(1));
    }
}