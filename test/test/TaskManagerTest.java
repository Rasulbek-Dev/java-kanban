package test;

import managers.ManagerValidationException;
import managers.TaskManager;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;
    protected Epic epic;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Subtask subtask3;
    protected Task task;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();

        // Подготовка тестовых данных
        epic = manager.createEpic(new Epic("Test Epic", "Description"));

        subtask1 = new Subtask("Subtask 1", "Description", epic.getId());
        subtask1.setStatus(TaskStatus.NEW);

        subtask2 = new Subtask("Subtask 2", "Description", epic.getId());
        subtask2.setStatus(TaskStatus.NEW);

        subtask3 = new Subtask("Subtask 3", "Description", epic.getId());
        subtask3.setStatus(TaskStatus.NEW);

        task = new Task("Test Task", "Description");
        task.setStatus(TaskStatus.NEW);
    }

    // Тесты для задач
    @Test
    void shouldCreateTask() {
        Task createdTask = manager.createTask(task);
        assertNotNull(createdTask, "Задача должна быть создана");
        assertTrue(createdTask.getId() > 0, "Задача должна иметь ID > 0");
    }

    @Test
    void shouldGetTaskById() {
        Task createdTask = manager.createTask(task);
        Task foundTask = manager.getTask(createdTask.getId());

        assertNotNull(foundTask, "Задача должна быть найдена");
        assertEquals(createdTask.getId(), foundTask.getId(), "ID должны совпадать");
    }

    @Test
    void shouldUpdateTask() {
        Task createdTask = manager.createTask(task);
        createdTask.setTitle("Updated Title");
        createdTask.setDescription("Updated Description");
        createdTask.setStatus(TaskStatus.DONE);

        manager.updateTask(createdTask);
        Task updatedTask = manager.getTask(createdTask.getId());

        assertEquals("Updated Title", updatedTask.getTitle(), "Название должно обновиться");
        assertEquals("Updated Description", updatedTask.getDescription(), "Описание должно обновиться");
        assertEquals(TaskStatus.DONE, updatedTask.getStatus(), "Статус должен обновиться");
    }

    @Test
    void shouldDeleteTask() {
        Task createdTask = manager.createTask(task);
        manager.deleteTask(createdTask.getId());

        assertNull(manager.getTask(createdTask.getId()), "Задача должна быть удалена");
    }

    @Test
    void shouldGetAllTasks() {
        // Явно устанавливаем null для времени задач, чтобы избежать пересечения
        task.setStartTime(null);
        task.setDuration(null);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(null);
        task2.setDuration(null);

        manager.createTask(task);
        manager.createTask(task2);

        List<Task> tasks = manager.getAllTasks();
        assertEquals(2, tasks.size(), "Должно быть 2 задачи");
    }

// ,,,

    // Тесты для эпиков
    @Test
    void shouldCreateEpic() {
        assertNotNull(epic, "Эпик должен быть создан");
        assertTrue(epic.getId() > 0, "Эпик должен иметь ID > 0");
    }

    @Test
    void shouldGetEpicById() {
        Epic foundEpic = manager.getEpic(epic.getId());
        assertNotNull(foundEpic, "Эпик должен быть найден");
        assertEquals(epic.getId(), foundEpic.getId(), "ID должны совпадать");
    }

    @Test
    void shouldUpdateEpic() {
        epic.setTitle("Updated Epic Title");
        epic.setDescription("Updated Epic Description");

        manager.updateEpic(epic);
        Epic updatedEpic = manager.getEpic(epic.getId());

        assertEquals("Updated Epic Title", updatedEpic.getTitle(), "Название эпика должно обновиться");
        assertEquals("Updated Epic Description", updatedEpic.getDescription(), "Описание эпика должно обновиться");
    }

    @Test
    void shouldDeleteEpic() {
        manager.deleteEpic(epic.getId());
        assertNull(manager.getEpic(epic.getId()), "Эпик должен быть удален");
    }

    @Test
    void shouldGetAllEpics() {
        Epic epic2 = manager.createEpic(new Epic("Epic 2", "Description"));

        List<Epic> epics = manager.getAllEpics();
        assertEquals(2, epics.size(), "Должно быть 2 эпика");
    }

    @Test
    void shouldDeleteAllEpics() {
        manager.createEpic(new Epic("Epic 2", "Description"));
        manager.deleteAllEpics();

        assertTrue(manager.getAllEpics().isEmpty(), "Все эпики должны быть удалены");
        assertTrue(manager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены при удалении эпиков");
    }

    // Тесты для подзадач
    @Test
    void shouldCreateSubtask() {
        Subtask createdSubtask = manager.createSubtask(subtask1);
        assertNotNull(createdSubtask, "Подзадача должна быть создана");
        assertTrue(createdSubtask.getId() > 0, "Подзадача должна иметь ID > 0");
        assertEquals(epic.getId(), createdSubtask.getEpicId(), "Epic ID должен соответствовать");
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask invalidSubtask = new Subtask("Invalid", "Description", 9999);
        assertNull(manager.createSubtask(invalidSubtask), "Нельзя создать подзадачу без существующего эпика");
    }

    @Test
    void shouldGetSubtaskById() {
        Subtask createdSubtask = manager.createSubtask(subtask1);
        Subtask foundSubtask = manager.getSubtask(createdSubtask.getId());

        assertNotNull(foundSubtask, "Подзадача должна быть найдена");
        assertEquals(createdSubtask.getId(), foundSubtask.getId(), "ID должны совпадать");
    }

    @Test
    void shouldUpdateSubtask() {
        Subtask createdSubtask = manager.createSubtask(subtask1);
        createdSubtask.setTitle("Updated Subtask");
        createdSubtask.setStatus(TaskStatus.DONE);

        manager.updateSubtask(createdSubtask);
        Subtask updatedSubtask = manager.getSubtask(createdSubtask.getId());

        assertEquals("Updated Subtask", updatedSubtask.getTitle(), "Название подзадачи должно обновиться");
        assertEquals(TaskStatus.DONE, updatedSubtask.getStatus(), "Статус подзадачи должен обновиться");
    }

    @Test
    void shouldDeleteSubtask() {
        Subtask createdSubtask = manager.createSubtask(subtask1);
        manager.deleteSubtask(createdSubtask.getId());

        assertNull(manager.getSubtask(createdSubtask.getId()), "Подзадача должна быть удалена");
    }

    @Test
    void shouldGetAllSubtasks() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(2, subtasks.size(), "Должно быть 2 подзадачи");
    }

    @Test
    void shouldDeleteAllSubtasks() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        manager.deleteAllSubtasks();
        assertTrue(manager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
    }

    @Test
    void shouldGetSubtasksByEpic() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        List<Subtask> epicSubtasks = manager.getSubtasksByEpic(epic.getId());
        assertEquals(2, epicSubtasks.size(), "Должно быть 2 подзадачи у эпика");
    }

    // Тесты статуса эпика (граничные условия из ТЗ)
    @Test
    void epicStatusShouldBeNewWhenNoSubtasks() {
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic savedEpic = manager.getEpic(epic.getId());
        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "Статус эпика со всеми подзадачами NEW должен быть NEW");
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic savedEpic = manager.getEpic(epic.getId());
        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "Статус эпика со всеми подзадачами DONE должен быть DONE");
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksNewAndDone() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.DONE);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic savedEpic = manager.getEpic(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(),
                "Статус эпика с подзадачами NEW и DONE должен быть IN_PROGRESS");
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        // Явно устанавливаем null для времени подзадач, чтобы избежать пересечения
        subtask1.setStartTime(null);
        subtask1.setDuration(null);
        subtask2.setStartTime(null);
        subtask2.setDuration(null);

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.NEW);

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic savedEpic = manager.getEpic(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(),
                "Статус эпика с любой подзадачей IN_PROGRESS должен быть IN_PROGRESS");
    }

    // Тесты времени эпика
    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        LocalDateTime now = LocalDateTime.now();

        subtask1.setStartTime(now.plusHours(1));
        subtask1.setDuration(Duration.ofMinutes(30));

        subtask2.setStartTime(now.plusHours(2));
        subtask2.setDuration(Duration.ofMinutes(45));

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic savedEpic = manager.getEpic(epic.getId());

        // Базовая проверка - время должно быть не null
        assertNotNull(savedEpic.getStartTime(), "StartTime эпика не должен быть null");
        assertNotNull(savedEpic.getDuration(), "Duration эпика не должен быть null");
        assertNotNull(savedEpic.getEndTime(), "EndTime эпика не должен быть null");
    }

    @Test
    void epicTimeShouldBeNullWithNoSubtasks() {
        Epic savedEpic = manager.getEpic(epic.getId());

        assertNull(savedEpic.getStartTime(), "StartTime эпика без подзадач должен быть null");
        assertNull(savedEpic.getDuration(), "Duration эпика без подзадач должен быть null");
        assertNull(savedEpic.getEndTime(), "EndTime эпика без подзадач должен быть null");
    }

    // Тесты приоритетных задач
    @Test
    void shouldReturnPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now.plusHours(2));
        task1.setDuration(Duration.ofMinutes(30));

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusHours(1));
        task2.setDuration(Duration.ofMinutes(45));

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size(), "Должно быть 2 задачи в приоритетном списке");
        assertEquals("Task 2", prioritized.get(0).getTitle(),
                "Первой должна быть задача с более ранним StartTime");
        assertEquals("Task 1", prioritized.get(1).getTitle(),
                "Второй должна быть задача с более поздним StartTime");
    }

    @Test
    void shouldNotIncludeTasksWithoutTimeInPrioritizedList() {
        Task taskWithTime = new Task("With Time", "Description");
        taskWithTime.setStartTime(LocalDateTime.now().plusHours(1));
        taskWithTime.setDuration(Duration.ofMinutes(30));

        Task taskWithoutTime = new Task("Without Time", "Description");
        // Явно устанавливаем null для времени, так как конструктор Task теперь задает значения по умолчанию
        taskWithoutTime.setStartTime(null);
        taskWithoutTime.setDuration(null);

        manager.createTask(taskWithTime);
        manager.createTask(taskWithoutTime);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(1, prioritized.size(), "В приоритетном списке должна быть только 1 задача");
        assertEquals("With Time", prioritized.get(0).getTitle(),
                "В списке должна быть только задача со временем");
    }
    // Тесты пересечения времени
    @Test
    void shouldDetectTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusMinutes(15)); // Пересекается с первой задачей
        task2.setDuration(Duration.ofMinutes(30));

        assertThrows(ManagerValidationException.class, () -> manager.createTask(task2),
                "Должно быть выброшено исключение при пересечении времени задач");
    }

    @Test
    void shouldNotDetectTimeOverlapForNonOverlappingTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(now.plusHours(1)); // Не пересекается
        task2.setDuration(Duration.ofMinutes(30));

        assertDoesNotThrow(() -> manager.createTask(task2),
                "Не должно быть исключения для непересекающихся задач");
    }

@Test
    void shouldNotDetectTimeOverlapForTasksWithoutTime() {
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(null); // Явно устанавливаем null
        task1.setDuration(null);  // Явно устанавливаем null

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(null); // Явно устанавливаем null
        task2.setDuration(null);  // Явно устанавливаем null

        manager.createTask(task1);

        assertDoesNotThrow(() -> manager.createTask(task2),
                "Не должно быть исключения для задач без времени");
    }

    // Тесты истории
    @Test
    void shouldAddTaskToHistory() {
        Task createdTask = manager.createTask(task);
        manager.getTask(createdTask.getId()); // Добавляем в историю

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "В истории должна быть 1 задача");
        assertEquals(createdTask.getId(), history.get(0).getId(),
                "Задача в истории должна соответствовать созданной задаче");
    }

    @Test
    void shouldNotAddDuplicatesToHistory() {
        Task createdTask = manager.createTask(task);

        // Добавляем несколько раз
        manager.getTask(createdTask.getId());
        manager.getTask(createdTask.getId());
        manager.getTask(createdTask.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "В истории не должно быть дубликатов");
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task createdTask = manager.createTask(task);
        manager.getTask(createdTask.getId()); // Добавляем в историю

        manager.deleteTask(createdTask.getId()); // Удаляем задачу

        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    void shouldMaintainHistoryOrder() {
        // Создаем задачи с явным указанием null для времени
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(null);
        task1.setDuration(null);
        Task createdTask1 = manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(null);
        task2.setDuration(null);
        Task createdTask2 = manager.createTask(task2);

        Task task3 = new Task("Task 3", "Description");
        task3.setStartTime(null);
        task3.setDuration(null);
        Task createdTask3 = manager.createTask(task3);

        // Добавляем в определенном порядке
        manager.getTask(createdTask2.getId());
        manager.getTask(createdTask1.getId());
        manager.getTask(createdTask3.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size(), "В истории должно быть 3 задачи");
        assertEquals(createdTask2.getId(), history.get(0).getId(), "Порядок истории должен сохраняться");
        assertEquals(createdTask1.getId(), history.get(1).getId(), "Порядок истории должен сохраняться");
        assertEquals(createdTask3.getId(), history.get(2).getId(), "Порядок истории должен сохраняться");
    }
}