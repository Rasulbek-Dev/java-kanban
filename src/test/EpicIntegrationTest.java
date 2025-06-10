package test;

import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicIntegrationTest {
    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.createEpic(new Epic("Epic", "Description"));
    }

    @Test
    void    epicStatusShouldUpdateWhenSubtaskChanged() {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));


        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void deletingEpicShouldRemoveSubtasks() {
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        manager.deleteEpic(epic.getId());

        assertNull(manager.getEpic(epic.getId()));
        assertNull(manager.getSubtask(subtask.getId()));
    }

    @Test
    void shouldNotAllowDirectEpicStatusChange() {
        epic.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.NEW, manager.getEpic(epic.getId()).getStatus());
    }
}