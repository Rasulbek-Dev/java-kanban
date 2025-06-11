package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "model.Subtask{id=" + getId() + ", title='" + getTitle() + "', epicId=" +
                epicId + ", status=" + getStatus() + "}";
    }
}