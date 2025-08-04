package test;

import model.Epic;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {
    @Test
    void newEpicShouldHaveEmptySubtasks() {
        Epic epic = new Epic("model.Epic", "Description");
        assertTrue(epic.getSubtaskIds().isEmpty(), "Новый эпик должен иметь пустой список подзадач");
    }

    @Test
    void shouldNotAllowManualStatusChange() {
        Epic epic = new Epic("model.Epic", "Description");
        epic.setStatus(TaskStatus.DONE);

        assertNotEquals(TaskStatus.DONE, epic.getStatus(),
                "Статус эпика нельзя менять вручную");
    }
}