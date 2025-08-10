package model;

import managers.TaskType;

public class Subtask extends Task {
    private int epicId;


    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public Subtask(Subtask other) {
        super(other);
        this.epicId = other.epicId;
    }

    public int getEpicId() {

        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", epicId=" + epicId +
                ", status=" + getStatus() + '}';
    }
}