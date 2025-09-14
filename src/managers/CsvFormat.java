package managers;

import model.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class CsvFormat {
    public static String getHeader() {
        return "id,type,name,status,description,duration,startTime,epic";
    }

    public static String toString(Task task) {
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
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "",
                task.getStartTime() != null ? task.getStartTime().toString() : "",
                epicId
        );
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) {
            throw new IllegalArgumentException("Недостаточно полей в строке: " + value);
        }

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        Duration duration = null;
        if (fields.length > 5 && !fields[5].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(fields[5]));
        }

        LocalDateTime startTime = null;
        if (fields.length > 6 && !fields[6].isEmpty()) {
            startTime = LocalDateTime.parse(fields[6]);
        }

        switch (type) {
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                if (fields.length < 8) {
                    throw new IllegalArgumentException("Для подзадачи отсутствует epicId: " + value);
                }
                int epicId = Integer.parseInt(fields[7]);
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }
}