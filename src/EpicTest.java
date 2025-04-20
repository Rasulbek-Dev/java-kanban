import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void newEpicShouldHaveEmptySubtasks() {
        Epic epic = new Epic("Epic", "Description");
        assertTrue(epic.getSubtaskIds().isEmpty(), "Новый эпик должен иметь пустой список подзадач");
    }

    @Test
    void shouldNotAllowManualStatusChange() {
        Epic epic = new Epic("Epic", "Description");
        epic.setStatus(TaskStatus.DONE);

        assertNotEquals(TaskStatus.DONE, epic.getStatus(),
                "Статус эпика нельзя менять вручную");
    }
}