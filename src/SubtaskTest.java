import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void shouldReturnCorrectEpicId() {
        Subtask subtask = new Subtask("Subtask", "Description", 1);
        assertEquals(1, subtask.getEpicId(), "Подзадача должна возвращать корректный Epic ID");
    }
}