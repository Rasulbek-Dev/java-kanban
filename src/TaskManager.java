import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private int nextId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();

    // методы для обычных задач
    public Task createTask(Task task) {
        task.setId(nextId);
        tasks.put(nextId, task);
        nextId++;
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    // методы для эпиков
    public Epic createEpic(Epic epic) {
        epic.setId(nextId);
        epics.put(nextId, epic);
        nextId++;
        return epic;
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    // методы для подзадач
    public Subtask createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            System.out.println("Ошибка: эпик с ID " + epicId + " не существует!");
            return null;
        }

        subtask.setId(nextId);
        subtasks.put(nextId, subtask);
        Epic epic = epics.get(epicId);
        epic.addSubtaskId(nextId);
        nextId++;
        updateEpicStatus(epic);
        return subtask;
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epics.get(subtask.getEpicId()));
        }
    }

    public List<Subtask> getSubtasksByEpic(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        }
        return result;
    }

    private void updateEpicStatus(Epic epic) {
        List<Subtask> subs = getSubtasksByEpic(epic.getId());

        if (subs.isEmpty()) {
            epic.updateStatus(TaskStatus.NEW);
            return;
        }

        int newCount = 0;
        int doneCount = 0;

        for (Subtask sub : subs) {
            if (sub.getStatus() == TaskStatus.NEW) newCount++;
            if (sub.getStatus() == TaskStatus.DONE) doneCount++;
        }

        if (doneCount == subs.size()) {
            epic.updateStatus(TaskStatus.DONE);
        } else if (newCount == subs.size()) {
            epic.updateStatus(TaskStatus.NEW);
        } else {
            epic.updateStatus(TaskStatus.IN_PROGRESS);
        }
    }
}