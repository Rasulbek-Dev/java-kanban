package managers;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CsvFormat.getHeader() + "\n");

            // Сохраняем задачи всех типов
            for (Task task : getAllTasks()) {
                writer.write(CsvFormat.toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(CsvFormat.toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(CsvFormat.toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length <= 1) return manager;

            int maxId = 0;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) continue;
                try {
                    Task task = CsvFormat.fromString(lines[i]);
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }
                    if (task instanceof Epic) {
                        manager.createEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.createSubtask((Subtask) task);
                    } else {
                        manager.createTask(task);
                    }
                } catch (Exception e) {
                    throw new ManagerSaveException("Ошибка загрузки: повреждённая строка \"" + lines[i] + "\"", e);
                }
            }
            InMemoryTaskManager.nextId = maxId + 1;

            // После загрузки всех задач обновляем статусы эпиков
            manager.updateAllEpics();
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