package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import java.util.List;

public interface TaskManager{
    Task createTask(Task task);

    List<Task> getAllTasks();

    void deleteTask(int id);

    Task getTask(int id);

    void updateTask(Task task); // Добавлен новый метод

    Epic createEpic(Epic epic);

    List<Epic> getAllEpics();

    void deleteEpic(int id);

    Epic getEpic(int id);

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    List<Subtask> getSubtasksByEpic(int epicId);

    Subtask getSubtask(int id);

    List<Task> getHistory();
}