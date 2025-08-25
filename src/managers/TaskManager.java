package managers;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {

    Task createTask(Task task);

    List<Task> getAllTasks();

    void deleteTask(int id);

    void deleteAllTasks();

    Task getTask(int id);

    void updateTask(Task task);

    Epic createEpic(Epic epic);

    List<Epic> getAllEpics();

    void deleteEpic(int id);

    void deleteAllEpics();

    Epic getEpic(int id);

    void updateEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    void deleteAllSubtasks();

    List<Subtask> getSubtasksByEpic(int epicId);

    List<Subtask> getAllSubtasks();

    Subtask getSubtask(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    boolean hasTimeOverlap(Task task);
}