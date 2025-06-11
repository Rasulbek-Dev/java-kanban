package managers;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final List<Task> tasks = new ArrayList<>();
    private final List<Epic> epics = new ArrayList<>();
    private final List<Subtask> subtasks = new ArrayList<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public Task createTask(Task task) {
        task.setId(nextId++);
        tasks.add(task);
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    @Override
    public void deleteTask(int id) {
        Task taskToRemove = null;
        for (Task task : tasks) {
            if (task.getId() == id) {
                taskToRemove = task;
                break;
            }
        }
        if (taskToRemove != null) {
            tasks.remove(taskToRemove);
            historyManager.remove(id);
        }
    }

    @Override
    public Task getTask(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                historyManager.add(task);
                return task;
            }
        }
        return null;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.add(epic);
        return epic;
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epicToRemove = null;
        for (Epic epic : epics) {
            if (epic.getId() == id) {
                epicToRemove = epic;
                break;
            }
        }

        if (epicToRemove != null) {

            List<Subtask> subtasksToRemove = new ArrayList<>();
            for (Subtask subtask : subtasks) {
                if (subtask.getEpicId() == epicToRemove.getId()) {
                    subtasksToRemove.add(subtask);
                    historyManager.remove(subtask.getId());
                }
            }
            subtasks.removeAll(subtasksToRemove);

            epics.remove(epicToRemove);
            historyManager.remove(id);
        }
    }

    @Override
    public Epic getEpic(int id) {
        for (Epic epic : epics) {
            if (epic.getId() == id) {
                historyManager.add(epic);
                return epic;
            }
        }
        return null;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        boolean epicExists = false;
        for (Epic epic : epics) {
            if (epic.getId() == subtask.getEpicId()) {
                epicExists = true;
                break;
            }
        }

        if (!epicExists) {
            System.out.println("Ошибка: эпик с ID " + subtask.getEpicId() + " не существует!");
            return null;
        }

        subtask.setId(nextId++);
        subtasks.add(subtask);

        for (Epic epic : epics) {
            if (epic.getId() == subtask.getEpicId()) {
                epic.addSubtaskId(subtask.getId());
                updateEpicStatus(epic);
                break;
            }
        }

        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).getId() == subtask.getId()) {
                subtasks.set(i, subtask);

                for (Epic epic : epics) {
                    if (epic.getId() == subtask.getEpicId()) {
                        updateEpicStatus(epic);
                        break;
                    }
                }
                return;
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        List<Subtask> result = new ArrayList<>();
        for (Subtask subtask : subtasks) {
            if (subtask.getEpicId() == epicId) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public Subtask getSubtask(int id) {
        for (Subtask subtask : subtasks) {
            if (subtask.getId() == id) {
                historyManager.add(subtask);
                return subtask;
            }
        }
        return null;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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