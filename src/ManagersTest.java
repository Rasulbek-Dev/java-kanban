import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void shouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");
    }

    @Test
    void shouldReturnInitializedHistoryManager() {
        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history, "Менеджер истории не должен быть null");
    }
}