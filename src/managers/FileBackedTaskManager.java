package managers;

import model.Epic;
import model.Subtask;
import model.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CsvFormat.getHeader() + "\n");

            for (Task task : getAllTasks()) {
                writer.write(CsvFormat.toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(CsvFormat.toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(CsvFormat.toString(subtask) + "\n");
            }

            writer.write("\n");
            writer.write(historyToString(getHistory()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения", e);
        }
    }

    private String historyToString(List<Task> history) {
        if (history.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Task task : history) {
            sb.append(task.getId()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();
        if (value != null && !value.isBlank()) {
            String[] ids = value.split(",");
            for (String id : ids) {
                historyIds.add(Integer.parseInt(id.trim()));
            }
        }
        return historyIds;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length <= 1) return manager;

            int emptyLineIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].isBlank()) {
                    emptyLineIndex = i;
                    break;
                }
            }

            int maxId = 0;
            for (int i = 1; i < emptyLineIndex; i++) {
                if (lines[i].isBlank()) continue;
                try {
                    Task task = CsvFormat.fromString(lines[i]);
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        Subtask subtask = (Subtask) task;
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                } catch (Exception e) {
                    throw new ManagerSaveException("Ошибка загрузки: повреждённая строка \"" + lines[i] + "\"", e);
                }
            }
            InMemoryTaskManager.nextId = maxId + 1;

            manager.updateAllEpics();

            if (emptyLineIndex != -1 && emptyLineIndex + 1 < lines.length) {
                String historyLine = lines[emptyLineIndex + 1];
                if (!historyLine.isBlank()) {
                    List<Integer> historyIds = manager.historyFromString(historyLine);
                    for (Integer id : historyIds) {
                        Task task = manager.tasks.get(id);
                        if (task == null) task = manager.epics.get(id);
                        if (task == null) task = manager.subtasks.get(id);
                        if (task != null) {
                            manager.historyManager.add(task);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }
        return manager;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}