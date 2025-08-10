package model;

import managers.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public Epic(String title, String description) {

        super(title, description);
    }

    public Epic(Epic other) {
        super(other);
        this.subtaskIds = new ArrayList<>(other.subtaskIds);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void updateStatus(TaskStatus taskStatus) {
        super.setStatus(taskStatus);
    }

    @Override
    public void setStatus(TaskStatus taskStatus) {
        System.out.println("Ошибка: статус эпика нельзя менять вручную!");

    }
}