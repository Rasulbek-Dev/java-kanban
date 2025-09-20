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

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private String toString(Task task) {
        String epicId = "";
        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
        );
    }

    private Task fromString(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) {
            throw new IllegalArgumentException("Недостаточно полей в строке: " + value);
        }

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                if (fields.length < 6) {
                    throw new IllegalArgumentException("Для подзадачи отсутствует epicId: " + value);
                }
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
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
                    Task task = manager.fromString(lines[i]);
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
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }
}